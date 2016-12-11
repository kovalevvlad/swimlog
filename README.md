# Swim Log
This is a toy webapp that I have created to learn a bit of AngularJS and polish off my Jersey skills. The goal of the application is to let users keep a log of their swimming history. The project consists of two parsts - the backend and the frontend.
### Application Overview
 - Users are able to create accounts for tracking their swimming information.
 - When logged in, users are able to create, delete and update their own swimming logs
 - Users can take one of three available roles - user/admin/manager. A regular user is only able to access their own records, a manager is able to CRUD users, and an admin is able to CRUD all swimming records and users.
 - Each swimming log entry has a date, distance, and time and an average speed
 - Users are able to filter the log by dates
 - Each user has some basic statistics displayed for their log
### Backend
The backend is written in Java and using the Jersey framework. I am using H2 for data storage. The backend is completely RESTful, all operations are performed via REST endpoints.
### Running the App
clone the project to a directory of your choice `<dir>`
`cd <dir>`
`mvn exec:java -Dexec.mainClass="london.stambourne6.swimlog.Server"`
This will get the backend running on http://localhost:9090
Now let's move to the frontend directory:
`cd <dir>/frontend`
We will need to serve the frontend via a webserver, I like python's SimpleHTTPServer for its simplicity:
`python -m SimpleHTTPServer`
Now you can access the frontend via http://localhost:8000/webapp/. When the app is run for the first time, it is set up with an admin user to let you check out the admin functionality. The username is "admin" and the password is "changeme".
