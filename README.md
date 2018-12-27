## Hadoop과 ES-Hadoop 연동 Example

>apache-hadoop 2.7.x 버전 사용

standard-alone 으로 설치했고, 여기서 설치과정은 생략한다. 단 하둡설정파일은
resoucre/hadoop안에 위치시켰다. 

### 1. 참고자료 

MapReduce 용 참고 데이터는 `시작하세요 하둡 프로그래밍(정재화/위키북스)` 중에서 ASA(미국항공편운항통계데이터)를 참고하였다. 
샘플데이터는 용량이 너무 큰 관계로 git에 업로드하지는 않았다. 

### 2. build.gradle 

사용하는 라이브러리는 다음과 같다. 
 
```
compile 'org.elasticsearch:elasticsearch-hadoop:6.5.3'    
compile 'org.elasticsearch:elasticsearch-hadoop-mr:6.5.3'    
        
compile group: 'org.apache.hadoop', name: 'hadoop-mapreduce-client-core', version: '2.7.7'
compile group: 'org.apache.hadoop', name: 'hadoop-common', version: '2.7.7'    
```    
 
 ### 3. 주요 클래스 설명

 #### AirlinePerformanceParser
 csv 파일을 파싱처리할 때 사용하는 클래스이다. split를 이용해서 각 컬럼별로 구분하여 분리하고, 컬럼등을 용도에 맞게 조합하는 역할을 한다. 

 #### DelayCounters
 enum 클래스이다. Mapper 클래스에서 사용한다. 

 #### DelayCountMapper
 mapper 클래스이다. Mapper 클래스는 csv파일을 읽어들여 위에 AirlinePerformanceParser를 이용해서 Mapper 데이터를 만든다.

 #### DelayCountReducer
 reducer 클래스이다. mapper클래스에서 만든 데이터를 reduce하는 역할을 한다. 


#### DelayCountReducerToElastic 
 역시 reducer 클래스이다. 하지만 elasticsearch로 데이터를 전달하기위해서 출력값을 `MapWritable` 으로 저장한다. 

#### DelayC둡ounterMain
MapReduce를 처리하는 메인클래스 args에 input, output경로를 통해 처리할 수 있다.

#### DelayCountReducerToElastic
MapReduce를 처리하는 메인클래스 args에 input에 있는 데이타를 map 처리후 reduce 결과를 elasticsearch에 전송한다. 


### 4. 하둡

하둡데이터를 포멧
```
./bin/hdfs namendoe -format
```
hadoop input 디렉토리 생성
```
./bin/hdfs dfs -mkdir -p input
```
샘플데이터를 input에 저장 (/hodoop/sample/dataexpo)
```
./bin/hdfs dfs -put data sample/dataexpo/*.csv input
```
빌드한 다음에 DelayC둡ounterMain 실행하여 reduce 파일 확인
```
./bin/hadoop jar eshadoop-example.jar com.example.DelayCounterMain input delay_count_mos
```
실행후 잘 생성 되었는지 조회 
```
./bin/hdfs dfs -ls /user/ykkim/delay_count_mos
```
아래와 같이 생성되면 성공한 것이다. 

```
-rw-r--r--   1 ykkim supergroup          0 2018-12-27 15:04 /user/ykkim/delay_count_mos/_SUCCESS
-rw-r--r--   1 ykkim supergroup       3570 2018-12-27 15:04 /user/ykkim/delay_count_mos/arrival-r-00000
-rw-r--r--   1 ykkim supergroup       3569 2018-12-27 15:04 /user/ykkim/delay_count_mos/departure-r-00000
-rw-r--r--   1 ykkim supergroup          0 2018-12-27 15:04 /user/ykkim/delay_count_mos/part-r-00000
```


### 5. Elasticsearch로  Reduce 결과 전송 

Elastic에 전송하기 위해서 먼저 index를 생성해준다.

```json 
PUT /eshadoop_delay/
{
  "mappings": {
    "_doc": {
      "properties": {
      	"delayType": {
      		"type": "keyword"
      	},
        "yearMonth": {
          "type": "date",
          "format": "yyyyMM"
        },
        "count": {
          "type": "integer"
        }
      }
    }
  }
}
```
이제 빌드후에 하둡에서 `DelayCounterElasticMain`를 실행하자. INPUT만 적용하면 된다. OUTPUT는 엘라스틱으로 소스에 세팅이 되어 있으므로...

```
./bin/hadoop jar eshadoop-example.jar com.example.DelayCounterElasticMain input
```
잘 완료되면 엘라스틱에서 조회해보면 된다. 
