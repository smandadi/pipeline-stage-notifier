## Pipeline-Stage-Notifier

This is an example script which is capable of sending build status notifcations from each stage/parallel of Jenkinspipeline to Bitbucket PR.
* generator.py - Generates all JSON files with stage and build info, which are later used by "curl" to call Bitbucket REST APIs to update build result
* pipeline.groovy: A simple pipeline file with one stage as an example
 
 ![Alt Text](https://media.giphy.com/media/u4aV0oxp3Rq058SAlJ/giphy.gif)