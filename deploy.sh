#!/usr/bin/env bash

heroku login
heroku deploy:jar build/libs/pokertable-0.0.1-SNAPSHOT.jar --app vangospokertable
