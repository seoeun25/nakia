# panther
---
Lezhin Payment Gateway System

> [Detail Document](https://wiki.lezhin.com/display/BIZDEV/Panther)

## Quick Start
```console
$ git clone https://github.com/lezhin/panther.git
$ gradle clean build
$ java -Dspring.profiles.active=local -jar build/libs/panther-${version}.jar 
$ curl http://localhost:8081/health
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

