(ns sparkling.rdd.hadoopAvro
  (:require [sparkling.api :as s]
            [clojure.string :as string])
  (:import
    [org.apache.hadoop.io NullWritable]
    [abracad.avro ClojureData]
    [sparkling.hadoop ClojureAvroInputFormat]
    [scala Tuple2]
    [org.apache.spark.api.java JavaSparkContext]
    [org.apache.avro.hadoop.io AvroSerialization]))



(defn key-only [^Tuple2 item]
  (._1 item))

(defn load-avro-file
  "This get's me a vector of maps from the avro file."
  [^JavaSparkContext sc path & {:keys [requires]}]
  (let [conf (.hadoopConfiguration sc)]
    (AvroSerialization/setDataModelClass conf ClojureData)
    (when requires
      (.set conf ClojureAvroInputFormat/REQUIRED_NAMESPACES (string/join "," (map name requires))))
    (s/map (.newAPIHadoopFile sc
                              path
                              ClojureAvroInputFormat
                              Object
                              NullWritable
                              conf)
           key-only)))



;; this is not working right now:
#_(defn save-avro-file
  [^JavaSparkContext sc rdd schema path]
  (let [conf (.hadoopConfiguration sc)
        job (Job. conf)]
    (AvroSerialization/setDataModelClass conf ClojureData)
    (AvroJob/setOutputKeySchema job schema)
    (.saveAsNewAPIHadoopFile (s/map-to-pair rdd (fn [x] (s/tuple x nil)))
                             path
                             Object
                             NullWritable
                             AvroKeyOutputFormat
                             (.getConfiguration job))
    ))




#_(def rdd (sparkling.rdd.hadoopAvro/load-avro-file scontext "hdfs://hdfs-master:8020/data/part-m-00000.avro"))
#_(def prdd (sparkling.api/map-to-pair rdd (sparkling.api/fn [item] [(org.apache.avro.mapred.AvroKey. item) nil])))
#_(sparkling.rdd.hadoopAvro/save-avro-file
  scontext
  prdd
  utils.avro-schemas/my-schema
  "hdfs://hdfs-master:8020/data/tmp/test")

