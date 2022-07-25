# TaskTracker :clipboard:
Task management tool for teams or personal use.

## Description :memo:
This project was developed as part of the **Netcracker** training center (spring 2022). :cookie:

Client-server browser application written in **`Java 15`** using the **`Spring`** framework.

The application has MVCS (Model - View - Controller - Service) architecture. The service layer contains the business logic. Controllers have access to the service layer, and the service layer processes data and communicates with the DAO layer.

## Deployment :whale:

The application can be run using **`Docker`**. The application has `Dockerfile` and `docker-compose.yml`.
You can use the following commands to run docker containers:

```
mvn clean package
```

```
docker-compose build
```

```
docker-compose up
```

Expected result:

![image](https://user-images.githubusercontent.com/62648024/180817621-48558a0e-3840-46bc-8f14-7dcf9b8d3afa.png)


## Sign in | Sign up :rocket:
User login and registration pages:

![image](https://user-images.githubusercontent.com/62648024/180289134-752928b2-a43f-45b3-b7fa-e97fdafb311c.png)

![image](https://user-images.githubusercontent.com/62648024/180289195-55b02f54-d383-46e6-9cc6-1f08b5b1f08f.png)

## Main page :card_file_box:
Main page with a list of all tasks. Tasks are displayed sorted by status. If you are currently working on a task (marked as active), then it is displayed first in the list.

:exclamation: **`Warning!`** The user cannot work on more than one task at a time. If the user already has an active task and clicks 'activate' for another task, then the first task will become inactive and the second one will be activated. The active task is also deactivated when the user logs out via the button.

![image](https://user-images.githubusercontent.com/62648024/180293354-6121f46f-320f-4e80-b2e6-df9bc25e735f.png)

Using the buttons **`All`**, **`Assigned`**, **`Created`**, the user can highlight certain groups of tasks.
- Button **`All`** - deselect tasks
- Button **`Assigned`** - highlights in yellow the tasks that are assigned to you
- Button **`Created`** - highlights in blue the tasks created by you

![image](https://user-images.githubusercontent.com/62648024/180294653-8178a937-182e-4818-9b01-a48573f5a996.png)

![image](https://user-images.githubusercontent.com/62648024/180295170-02c53dc5-56a8-48c7-a5dc-7e551e408604.png)

## Task :file_folder:

You can open a task and view its description in more detail. If the user is the creator of the task, then he can change the user-executor and a due date. He can also cancel it by setting the status to **`CANCELED`**. If the user has administrator rights, then he can change these parameters for any task. 

On the same page there is a button **`Create subtask`**. Clicking the button will redirect the user to the create a new task page.

![image](https://user-images.githubusercontent.com/62648024/180295630-c556dc70-b2cd-48e3-b1d0-100714b5f4e8.png)

## Creating task :crayon: 

If you need to create a task, you can do this by going to the creation page from the main one.

When creating a task, the fields "name" and "description" are required.

![image](https://user-images.githubusercontent.com/62648024/180297309-c903e392-0dc5-4671-9d30-d5deacc86e55.png)

## Created tasks | Assigned tasks :gear:

If the user does not want to view the list of all tasks, then he can select one of the sections with a certain type of task.

![image](https://user-images.githubusercontent.com/62648024/180300849-25e87bd6-3c9d-4232-9989-860fd6ba59fb.png)

## About statuses :dna:

Each task can have one of five statuses:
- :black_circle: **`TODO`**
- :yellow_circle: **`ACCEPTED`**
- :large_blue_circle: **`IN PROGRESS`**
- :green_circle: **`DONE`**
- :red_circle: **`CANCELED`**

More about each status:
- **`TODO`** - The status is assigned to any task automatically when it is created.
- **`ACCEPTED`** - The task changes from the **`TODO`** status to the **`ACCEPTED`** status when the user clicks the button 'Accept'. This means that the user has seen the task assigned to him and will be planning to work on it.
- **`IN PROGRESS`** - The status is assigned to the task when a user started working on it for the FIRST TIME (pressed the button 'Activate').
- **`DONE`** - The status **`DONE`** means that the user has finished working on the task.
- **`CANCELED`** - The **`CANCELED`** status means that this task has been canceled.

A user with the USER role can usually only do the following: change the status of a task from **`IN PROGRESS`** to **`DONE`**.
Also, a user with the USER role can change the status to **`CANCELED`** in any moment if he is the CREATOR of this task.

A user with the ADMIN role can change the status of any task to **`IN PROGRESS`**, **`DONE`**, **`CANCELED`** at any time.

## Statistics :bar_chart:

The 'statistics' section allows you to see the duration of work on tasks.

First, you need to select the start and finish of the period for which you want to get statistics. You can also select none of the dates, or select only one.

Then the user can select the task for which he wants to get statistics. He can get the time of work on only one specific task, or he can get the duration of work on a task, taking into account the work on all its subtasks.

If the user does not select a specific task, then he will get the duration of work on all tasks.

![image](https://user-images.githubusercontent.com/62648024/180304997-a202d9e9-666a-4de6-a877-b3923b1ea117.png)

![image](https://user-images.githubusercontent.com/62648024/180305107-b1bf316d-0c0c-49aa-935e-6e12a61fc07d.png)

![image](https://user-images.githubusercontent.com/62648024/180305201-a2b09568-5b79-4f23-86ea-0e9dbf655ea0.png)

In the drop-down list of tasks, the user displays those tasks on which the user has ever worked. Even if the user is not currently the executor for the task, but previously worked on it, it will be in the list.

If the user has not worked on tasks, then he will see the following in the statistics section:

![image](https://user-images.githubusercontent.com/62648024/180305823-f1ce829f-d7c9-4d06-9b1c-68051efffe0b.png)

For the administrator, the statistics section looks different. 
First, the administrator needs to select the user for which he wants to see statistics.

![image](https://user-images.githubusercontent.com/62648024/180319883-31052ec5-40c8-4b4d-b9fc-3e5d8af410a9.png)

After selecting a user, the administrator will already be able to select parameters for statistics for a particular user.

## History :scroll:

In the History section, the user can view the history of task changes. When you go to this section, the user is shown a history of changes for the last three days.

The user can select the following options for filtering:
- Time interval
- Task type (created by user / assigned to user)
- Changed parameter

![image](https://user-images.githubusercontent.com/62648024/180320564-af971f90-873a-4541-8e08-48de5f8fc268.png)

For the administrator, the section looks different. He has to select a specific user for which the administrator wants to get the history.

![image](https://user-images.githubusercontent.com/62648024/180321373-9e874287-29e6-4f99-a415-5321439474e7.png)

# Dependencies :link:
This application is written in **`Java 15`** using these libraries and tools:
- [Spring Framework Boot 2.6.7](https://spring.io/projects/spring-boot)
- - [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- - [Spring Web MVC](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html)
- - [Spring Security](https://spring.io/projects/spring-security)
- [Thymeleaf 3.0.15](https://www.thymeleaf.org/)
- [PostgreSQL 14.2](https://www.postgresql.org/)
- [Lombok 1.18.24](https://projectlombok.org/)
- [log4j 1.2.12](https://logging.apache.org/log4j/2.x/index.html)
- [Docker Compose 3.9](https://docs.docker.com/compose/)

# Built With :round_pushpin:
- [Maven](https://maven.apache.org/) - Dependency Management
