CREATE CUSTOM INDEX IF NOT EXISTS users_index
ON test.users ()
USING 'com.stratio.cassandra.lucene.Index'
WITH OPTIONS = {
'refresh_seconds': '60',
'ram_buffer_mb': '64',
'max_merge_mb': '5',
'max_cached_mb': '30',
'excluded_data_centers': 'dc2,dc3',
'partitioner': '{type: "token", partitions: 4}',
'schema': '{
      analyzers: {
         my_custom_analyzer: {
            type: "snowball",
            language: "Spanish",
            stopwords: "el,la,lo,los,las,a,ante,bajo,cabe,con,contra"
         }
      },
      default_analyzer: "english",
      fields: {
         name: {type: "string"},
         gender: {type: "string", validated: true},
         animal: {type: "string"},
         age: {type: "integer"},
         food: {type: "string"},
         number: {type: "integer"},
         bool: {type: "boolean"},
         date: {type: "date", validated: true, pattern: "yyyy/MM/dd"},
         duration: {type: "date_range", from: "start_date", to: "stop_date"},
         place: {type: "geo_point", latitude: "latitude", longitude: "longitude"},
         mapz: {type: "string"},
         setz: {type: "string"},
         listz: {type: "string"},
         phrase: {type: "text", analyzer: "my_custom_analyzer"}
      }
   }'
}