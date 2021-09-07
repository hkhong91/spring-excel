# Spring excel

Let's process one million row excel file.

### upload at once

* Memory Usage 1,835mb
* CPU Usage 42% (2.7 GHz quad-core Intel Core i7)

|Task|sec|%|
|------|---|---|
|CreateWorkbook|20.410314497|048%|
|SetProducts|5.127343926|012%|
|InsertProducts|17.357501694|040%|

### download at once

* Memory Usage 478mb
* CPU Usage 25% (2.7 GHz quad-core Intel Core i7)

|Task|sec|%|
|------|---|---|
|FetchProducts|18.194994143|067%|
|SetProducts|7.058560845|026%|
|CreateStream|1.881065509|007%|