#!/bin/bash

java -Djava.library.path="$PATH:./lib/OpenCV/:./lib/OpenNI/:./lib/NiTE/" -cp "./bin:./lib/*:./lib/Jena/*:./lib/Jung/*:./lib/NiTE/*:./lib/OpenNI/*:./lib/OpenCV/*" it.polito.computervision.controller.Main $@

