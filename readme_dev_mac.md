How to set up kdb+ Studio for Mac developers
=========

Prep 
  - Register and wait for an email from [Kx Q Service]
  - In the email you'll receive a download link to ```kdb for MacOs``` and the licence key. 

How to set up Q service
  - Download Q service and unzip it to your root folder.
  - Place the licence key (```kc.lic```) into the q folder.
  - From your terminal start the service with ```q/m64/q -p 9989```
  - You should see a similar result
    - ```KDB+ 4.1 2024.04.29 Copyright (C) 1993-2024 Kx Systems```
      ```m64/ 10(24)core 16384MB [userid] [computer name].broadband [ip address] EXPIRE 2025.05.07 [email address] KDB PLUS TRIAL #*****```
  - To test it works type in ```1+1``` and you should get a result back ```2 ```
  - Tip: to exit Q service type in ```\\```

How to run KDB+ Studio
- This is a gradle project so head over to the gradle build tool.
- Build the project ```kdb-studio > Tasks > build > build```
- Run the project ```kdb-studio > Tasks > application > run```
- You can then use debug on the run task as with any other project.

How to connect KDB+ Studio to Q Service
- Once you start the project in the top left corner look for ```Server > Add```
- Type in any name, Host: ```localhost```, Port ```9989``` and click OK
- Type in ```1+2+3``` in the top dialog box and click ```Query > Execute```
- A result of ```8``` will appear in the bottom dialog box

Documentation
- [Q Service Commands]

[Kx Q Service]:https://kx.com/kdb-personal-edition-download/
[Q Service Commands]:https://code.kx.com/q/basics/ipc/
