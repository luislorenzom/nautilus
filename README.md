# Nautilus

### What is nautilus?
Nautilus is an open source, distributed web platform to enable protection and sharing of files, using peer-to-peer technologies. The top priority of this project is to save the user's files in a distributed and redundant way and doing so protecting his or her privacy. In addition, the program allows the user to donate part of the personal disk storage, which will be used by other users, in order to increase the limit of space usage allowed inside the network.

### Features

- All the files you share or host inside the platform are split and then encrypted for increased security
- You can choose to host the files with a download limit and/or an expiration date
- You can also set a release date for a file, when the file will become available to other users
- After a file is uploaded to the program, the client generates a key file for retrieval purposes, the only way to have access to such file
- The possibility of sharing space from the user's personal computer to save files from other people is also offered, being rewarded with more space available to use inside the network. Because of the encryption used, no access is allowed to third-party splits or files hosted in this way.  
- The program has been designed to avoid keeping any kind of data record about the users and their personal information, beyond the basic data needed to operate the system, thus looking for the creation of a zero-knowledge system.

### Version
0.1 Alpha

### Technologies

Nautilus uses a number of open source projects to work properly:

* [AngularJS] - MVC framework used to develop the front-end of the application
* [Twitter Bootstrap] - Framework to ease the design of the graphical interface using templates
* [jQuery] - JavaScript library to better manage the Document Object Model (DOM)
* [Hibernate] - Object-relational mapping framework for the Java language
* [Spring] - Application framework and inversion of control container for the Java platform
* [HSQLDB] - Relational database management system written in Java
* [BouncyCastle] - Collection of APIs used in cryptography
* [TomP2P] - Distributed hash table which provides a decentralized key-value infrastructure

### Installation

```sh
$ git clone [git-repo-url]
$ cd nautilus
$ mvn package -Dmaven.test.skip=true
$ cd target
$ mv nautilus-0.1-SNAPSHOT-jar-with-dependencies.jar ~/nautilus
```

### How to use it?

#### Server mode
With this mode you allow to other users save their encrypted fragments of file in your storage folder.

First of all, if you want to launch a peer in server mode you must enable the service and set the space that you want to "give away" (in KB) in the config.xml. This file is generated in the same folder where the executable file is.
```xml
<serverAvailable>true</serverAvailable>
<limitSpace>1048576</limitSpace> <!-- 1GB -->
```
If you don't want to establish any limit, you can set the value to "-1"
```xml
<limitSpace>-1</limitSpace> <!-- No limit space -->
```
You can also change your storage folder in the config file. By default
this folder is created in your home folder. To change this, just write the path using the next tag.
 ```xml
 <!-- For example -->
 <StorageFolder>/media/user/2EFE01BBFE017BF9/nautilus_storage</StorageFolder>
 ```

Now you just run the service
```sh
$ java -jar nautilus-0.1-SNAPSHOT-jar-with-dependencies.jar -s
```
Finally, to stop the service just press Ctrl+C

#### Client mode
##### Save file
On the other hand we have the client mode, which can be used to save files in our server preference, and  to recover them afterwards using the generated keys.

In order to save our first file, we must define some server in the config file, like this:
```xml
<servers>
      <server>192.168.1.43</server>
</servers>
```
Once the server is set, we can then run the client to save the file. In this case we use the flag to
signal that no limit on the downloads is to be used.
```sh
$ java -jar nautilus-0.1-SNAPSHOT-jar-with-dependencies.jar -ck /home/user/Documents/file.pdf
```
If you want save your file with a limit, up to three downloads for example, you can use:
```sh
$ java -jar nautilus-0.1-SNAPSHOT-jar-with-dependencies.jar -ck file.pdf -dol 3
```
You can also establish one date to delete the file, once itis hosted in the network, using:
```sh
$ java -jar nautilus-0.1-SNAPSHOT-jar-with-dependencies.jar -ck file.pdf -dal 26/01/2016
```
On the other hand, if you want to establish a release date for the file, you need to use:
```sh
$ java -jar nautilus-0.1-SNAPSHOT-jar-with-dependencies.jar -ck file.pdf -dr 15/03/2016
```
Finally, for encrypt all the file keys with your public key, you need to use this flag:
```sh
$ java -jar nautilus-0.1-SNAPSHOT-jar-with-dependencies.jar -ck file.pdf -pkey
```
If you prefer to use another public key (For example: From another user) you must specify the key:
```sh
$ java -jar nautilus-0.1-SNAPSHOT-jar-with-dependencies.jar -ck file.pdf -pkey otherUserPKey.txt
```
All these commands are interchangeable, so you can combine the different flags when saving your files:
```sh
$ java -jar nautilus-0.1-SNAPSHOT-jar-with-dependencies.jar -ck file.pdf -dol 4 -dr 15/03/2016
```
##### Retrieval file
To retrieve the previously hosted file, we use the command "-cr" and the path to the generated file key:
```sh
$ java -jar nautilus-0.1-SNAPSHOT-jar-with-dependencies.jar -cr file1_key.xml
```
This will recover the file in the same folder where the program is executed

### TO-DOS

 - **Hold Punching**: Connection between two or more LAN networks
 - **A noise system**, in other words, a system that generates fake traffic to hide the real messages
 - **Synchronization between the different servers** in the network
 - Graphic interface
 - Client for Android smartphones
 - Testing in Windows systems

   [AngularJS]: <https://angularjs.org/>
   [Twitter Bootstrap]: <http://twitter.github.com/bootstrap/>
   [jQuery]: <http://jquery.com>
   [HSQLDB]: <http://hsqldb.org/>
   [Hibernate]: <http://hibernate.org/>
   [Spring]: <https://spring.io/>
   [TomP2P]: <http://tomp2p.net/>
   [BouncyCastle]: <https://www.bouncycastle.org/>
