# panther
---
Lezhin Payment Gateway System

> [Detail Document](https://wiki.lezhin.com/display/BIZDEV/Panther)

## Quick Start
```console
$ git clone https://github.com/lezhin/panther.git
$ gradle clean build
$ java -Dspring.profiles.active=local -jar build/libs/panther-${version}.jar 
$ curl https://localhost:9443/panther/version
```
* health check : [actuator](http://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#production-ready)

## Configuration

config 항목
* `internalPaymentUrl`: GCS의 default server.
* `datasource`: Persistence data store (Aurora)
* `cache`: Cache layer (Redis)
* `executor configs`: 각 Executor마다 필요로하는 설정값들

Spring profiles 사용
config 항목은 spring profiles를 이용하여 설정한다

* `production`: 실서비스용. AWS/EC2 실서버에서 실행. AWS/RDS 실서비스용 Aurora 사용. AWS/ElastiCache 사용
* `staging`: Staging용. AWS/EC2 beta서버에서 실행. AWS/RDS 실서비스용 Aurora 사용. AWS/ElastiCache 사용
* `qa`: QA용. AWS/EC2 beta서버에서 실행. AWS/RDS 실서비스용 Aurora 사용. AWS/ElastiCache 사용
* `beta`: DEV용. AWS/EC2 beta서버에서 실행. AWS/RDS 실서비스용 Aurora 사용. AWS/ElastiCache 사용
* `local`: 개발용. 로컬에서 실행. 로컬 mysql(or mariadb) 사용. 로컬 redis 사용
* `test`: 유닛 테스트용. 로컬에서 실행. h2 메모리 사용. JPA DDL create-drop. (redis는 ?)

## How to deploy


1. version.txt 를 수정하여 version을 업그레이드. 
예를 들면, version이 1.0.98일 경우 v1.0.98
2. docker image 빌드

    docker image를 빌드 하는 방법으로는 1. tag 사용 2.local build 두가지가 있다. 상황에 맞게 선택한다. 

    2.1. tag를 push 하면 travis를 통해 자동으로 docker image 생성되고 docker repo에 push 된다.
    tag naming은 버전 앞에 v를 붙인다. 예를 들면 v1.0.98 
    
    2.2. local에서는 다음과 같이 내장된 script를 이용하면 travis를 통하지 않고 바로 docker repo에 push 된다
    ```
    $ ./build_local.sh 1.0.98
    
    ```
3. deploy docker image
    
    slack의 panther_beta 채널에서 다음과 같이 입력하고 묻는 대로 하면 된다
    버전은 alpha, beta, qa, production에서 global로 사용한다. (어떤 docker image로 deploy하겠다)
    ```
    /ecs deploy panther
    원하는 infra 선택: 예를 들면 qa
    원하는 version 선택: 예를 들면 1.0.98
    ```
    
    docker image tags 확인: browser에서 다음과 같이
    ```
    https://docker.lezhin.com/v2/panther/tags/list
    ```
4. aws ecs 에서 instance 확인.
5. 배포 버전 확인
    ```
    https://qa-panther.lezhin.com/panther/version

    ```

