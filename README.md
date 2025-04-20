# Youtube Tutorials:
- https://www.youtube.com/watch?v=8X6uvKymTkM&t=202s
- https://www.youtube.com/watch?v=tXC9DQRWHUQ&t=24s

# ENV IntelliJ
- JWT = Just randam base64 string
- Mongo DB needs `user:password` and cluster name + db name after `/`
```
JWT_SECRET_BASE64=dENMZEhtNUF5dUN1aFZKY1JhR3NsdFJmemtBNjVnZGNoNVVXNEtUZA==;MONGODB_CONNECTION_STRING=mongodb+srv://user:password@cluster0.iv2js6b.mongodb.net/notes?retryWrites=true&w=majority&appName=Cluster0
```

# Create SSH (no passphrase needed)
```
geert@Thinkpad-Geert MINGW64 ~/.ssh
$ ssh-keygen -t ed25519 -b 4096 -C "Geert Berkers"

cat the public file and add it to hetzner creating VPS.

$ ssh root@91.99.8.239
Enter passphrase for key '/c/Users/geert/.ssh/id_ed25519':
Welcome to Ubuntu 24.04.2 LTS (GNU/Linux 6.8.0-57-generic x86_64)

 * Documentation:  https://help.ubuntu.com
 * Management:     https://landscape.canonical.com
 * Support:        https://ubuntu.com/pro

 System information as of Sun Apr 13 01:56:14 PM UTC 2025

  System load:  0.64              Processes:             128
  Usage of /:   3.0% of 37.23GB   Users logged in:       0
  Memory usage: 5%                IPv4 address for eth0: 91.99.8.239
  Swap usage:   0%                IPv6 address for eth0: 2a01:4f8:c013:e600::1


Expanded Security Maintenance for Applications is not enabled.

0 updates can be applied immediately.

Enable ESM Apps to receive additional future security updates.
See https://ubuntu.com/esm or run: sudo pro status


root@ubuntu-4gb-fsn1-2:~# ls
```

# Add admin user and grand permissions
```
adduser admin
uermod -aG sudo admin
cd /home/admin
mkdir .ssh
chown -R admin:admin .ssh
chmod 700 .ssh
cp /root/.ssh/authorized_keys /home/admin/.ssh
cd .ssh
chmod 600 authorized_keys
chown -R admin:admin authorized_keys

nano /etc/ssh/sshd_config
```
Change (#)PermitRootLogin to no (Not needed here)

## Create SSH alias
```
Host hetzner
  HostName 91.99.8.239
  User admin
  IdentityFile ~/.ssh/hetzner
  IdentitiesOnly yes
```

## Installing Java
```
sudo apt update && sudo apt upgrade
sudo apt-get install openjdk-21-jdk
```

## Push jar
```
./gradlew bootJar
copy absolute path in IntelliJ
scp "absolute_path.jar" hetzner:~/
lokaal -> remote
ssh hetzner
cd /opt
sudo mkdir notes

sudo mv "springboot_path.jar" /opt/notes/notes.jar
```

# Change execute
```chmod 700 notes.jar```

## Test jar
```java -jar notes.jar```
 
# Problem Environment variables occur...
But this is normal behaviour

## Create Service

```
cd /etc/systemd/system
sudo nano notes.service
```

// Notes.service systemctl
```
[Unit]
Description=Spring Boot Notes Application
After=network.target
    
[Service]
User=admin
Group=admin
EnvironmentFile=/etc/default/notes-env
ExecStart=/usr/bin/java -jar /opt/notes/notes.jar
Restart=always
RestartSec=5
    
[Install]
WantedBy=multi-user.target
```


## Start service
```
cd /etc/systemd/system
sudo systemctl deamon-reload
sydo systemctl enable notes.service
sudo start notes.service
```

## See logs:
```
sudo journal -n 200 -u notes.service -f
```

## SSL error? Fix IP from hetzner to mongoDB.


# Test via postman:

POST:
```
http://91.99.8.239:8085/auth/register
```

JSON payload:
```
{
    "email": "test@test.com",
    "password": "Test12345"
}
```

## Install NGINX
Config file:
```
server {
    server_name notes.gb-coding.nl; # replace with your domain

    location / {
        proxy_pass http://127.0.0.1:8085; # replace with your port applications.properties
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

## DNS instellen.
Ga naar Transip.
Ga naar gb-coding.nl
Voeg A DNS record toe met notes naar hetzner ip.
Wacht nu tot het live staat.

Daarna testen via:
http://notes.gb-coding.nl:8085/auth/register
Bij posten krijg je een conflict 409, komt omdat user al bestaat!
```
{
    "timestamp": "2025-04-20T21:59:36.212+00:00",
    "status": 409,
    "error": "Conflict",
    "path": "/auth/register"
}
```

## SSL Instellen
```
admin@ubuntu-4gb-fsn1-2:/etc/systemd/system$ sudo nano /etc/nginx/sites-available/notes
admin@ubuntu-4gb-fsn1-2:/etc/systemd/system$ cd /etc/nginx/sites-enabled/
#Create symlink
admin@ubuntu-4gb-fsn1-2:/etc/nginx/sites-enabled$ sudo ln -s /etc/nginx/sites-available/notes .
```

# Install SSL via certbot
```
admin@ubuntu-4gb-fsn1-2:/etc/nginx/sites-enabled$ sudo apt install certbot python3-certbot-nginx
admin@ubuntu-4gb-fsn1-2:/etc/nginx/sites-enabled$ sudo certbot --nginx -d notes.gb-coding.nl
email -> Y -> Y 
Test nu via de browser of postman.  https://notes.gb-coding.nl/auth/register
sudo systemctl reload nginx
```

## Update door commit
GitHub actions needs to restart, and use sudo.
But VPS will ask for password.
Change this using:


```
sudo visudo
admin ALL=(ALL) NOPASSWD: /usr/bin/systemctl restart notes.service
```
Github Action may fail! /opt/notes has root instead of admin


```
admin@ubuntu-4gb-fsn1-2:/etc/nginx/sites-enabled$ sudo visudo
[sudo] password for admin:
admin@ubuntu-4gb-fsn1-2:/etc/nginx/sites-enabled$ cd /opt/notes/
admin@ubuntu-4gb-fsn1-2:/opt/notes$ ls
notes.jar
admin@ubuntu-4gb-fsn1-2:/opt/notes$ ls -ld
drwxr-xr-x 2 root root 4096 Apr 16 15:19 .
admin@ubuntu-4gb-fsn1-2:/opt/notes$ sudo chown -R admin:admin .
admin@ubuntu-4gb-fsn1-2:/opt/notes$ ls -ld
drwxr-xr-x 2 admin admin 4096 Apr 16 15:19 .
admin@ubuntu-4gb-fsn1-2:/opt/notes$ ls
notes.jar
admin@ubuntu-4gb-fsn1-2:/opt/notes$ ls -lA
total 41072
-rw-r--r-- 1 admin admin 42050867 Apr 20 22:31 notes.jar
```

Now rerun github actions.

## Enable Firewall
```
admin@ubuntu-4gb-fsn1-2:/opt/notes$ sudo ufw status numbered
Status: inactive
admin@ubuntu-4gb-fsn1-2:/opt/notes$ sudo ufw default deny incoming
Default incoming policy changed to 'deny'
(be sure to update your rules accordingly)
admin@ubuntu-4gb-fsn1-2:/opt/notes$ sudo ufw default allow outgoing
Default outgoing policy changed to 'allow'
(be sure to update your rules accordingly)
admin@ubuntu-4gb-fsn1-2:/opt/notes$ sudo ufw allow 80/tcp
Rules updated
Rules updated (v6)
admin@ubuntu-4gb-fsn1-2:/opt/notes$ sudo ufw allow 443/tcp
Rules updated
Rules updated (v6)
admin@ubuntu-4gb-fsn1-2:/opt/notes$ sudo ufw allow 22/tcp
Rules updated
Rules updated (v6)
admin@ubuntu-4gb-fsn1-2:/opt/notes$ sudo ufw enable
Command may disrupt existing ssh connections. Proceed with operation (y|n)? y
Firewall is active and enabled on system startup
admin@ubuntu-4gb-fsn1-2:/opt/notes$ sudo ufw status numbered
Status: active

     To                         Action      From
     --                         ------      ----
[ 1] 80/tcp                     ALLOW IN    Anywhere
[ 2] 443/tcp                    ALLOW IN    Anywhere
[ 3] 22/tcp                     ALLOW IN    Anywhere
[ 4] 80/tcp (v6)                ALLOW IN    Anywhere (v6)
[ 5] 443/tcp (v6)               ALLOW IN    Anywhere (v6)
[ 6] 22/tcp (v6)                ALLOW IN    Anywhere (v6)
```
