# eventstreams-samples/Instructions.md

This document has instructions on the following:

1. How to create your Event Streams instance using the UI
2. How to create your Event Streams instance using the command line
3. Instructions on installing and using the Event Streams CLI


## Prerequisites

1. **If you don't already have one, create an Event Streams service instance.**

   1. Log in to the IBM Cloud console.
  
   2. Click **Catalog**.
   
   ![Catalog](https://github.ibm.com/kccox/eventstreams-samples/blob/main/java-apache/images/catalog.png)
   
    3. From the navigation pane, click **Integration**, click the **Event Streams** tile. The Event Streams service instance page opens.
    
  ![Integration](https://github.ibm.com/kccox/eventstreams-samples/blob/main/java-apache/images/Integration.png)
  
   4. For **Platform** choose Public Cloud and for **Location**, you can use the default, Dallas. For the plan, select the **Lite Plan**
   
   ![Platform](https://github.ibm.com/kccox/eventstreams-samples/blob/main/java-apache/images/Location.png)
   
   5. Enter a name for your service. You can use the default value.
   
  ![Name](https://github.ibm.com/kccox/eventstreams-samples/blob/main/java-apache/images/ESName.png)
  
   6. Click **Create**. The Event Streams **Getting started** page opens. 
  

2. **If you don't already have them, install the following prerequisites:**
	
	* Maven 
	* JDK 11 or higher

## Steps to Build the Sample

### **Navigating to your Event Streams instance**

   1. From the dashboard, navigate to the left side panel and look for **Resource List** and click on it
   

      ![IBM Cloud Dashboard](https://github.ibm.com/kccox/eventstreams-samples/blob/main/java-apache/images/dashboard.png)

   2. Look for Services and Software in the list of resources. Under this tab, look for the Event Streams instance that you created in the previous step
   
	
      ![ResourceList](https://github.ibm.com/kccox/eventstreams-samples/blob/main/java-apache/images/ResourceList.png)
        

   3. Once you click on your instance, you will be redirected to the Event Streams dashboard. Here you will be able to create topics, service credentials, etc. 
   

        ![ESDashboard](https://github.ibm.com/kccox/eventstreams-samples/blob/main/java-apache/images/ESDashboard.png)

### 1. **Create a topic**

   The topic is the core of Event Streams flows. Data passes through a topic from producing applications to consuming applications. 

   We'll be using the IBM Cloud console (UI) to create the topic, and will reference it when starting the application.

   1. Go to the **Topics** tab.
  
   2. Click **New topic**.
  
   3. Name your topic.
  
        > The sample application is configured to connect to topic `kafka-java-console-sample-topic`. If the topic does not exist, it is created when the application is started. 

   4. Keep the defaults set in the rest of the topic creation, click **Next** and then **Create topic**.

   5. The topic appears in the table in a few minutes. Congratulations, you have created a topic!
   
    	![Topics](https://github.ibm.com/kccox/eventstreams-samples/blob/main/java-apache/images/topics.png)
    
---

### 2. **Create credentials**

   To allow the sample application to access your topic, we need to create some credentials for it. 

   1. Go to **Service credentials** in the navigation pane.
  
   2. Click **New credential**.
  
   3. Give the credential a name so you can identify its purpose later. You can accept the default value.
  
   4. Give the credential the **Manager** role so that it can access the topics, and create them if necessary. 
  
   5. Click **Add**. The new credential is listed in the table in **Service credentials**.
  
   6. Click **View credentials** to see the `api_key` and `kafka_brokers_sasl` values.
   
    	![Credentials](https://github.ibm.com/kccox/eventstreams-samples/blob/main/java-apache/images/credentials.png)
    
---


## Using the CLI to create an Event Streams instance and Service Credentials


   Prerequisite: Please ensure you have the IBMCLOUD CLI installed. For more information, see [Getting started with the IBM Cloud CLI](https://cloud.ibm.com/docs/cli?topic=cli-getting-started)

   1. Login to IBM Cloud:

      `ibmcloud login -a cloud.ibm.com`

   2. Create an Event Streams instance

      `ibmcloud resource service-instance-create <INSTANCE_NAME> messagehub standard <REGION>`


      Region is us-south, eu-gb, etc. 
      Wait until your instance is completely provisioned, for the standard plan it is instantaneous

      You can use the `ibmcloud resource service-instance` command to check on your instance

   3. Create service credentials 

      `ibmcloud resource service-key-create <service-instance-key-name> Manager --instance-name <service-instance-name>`

      - <service-instance-key-name>  the name of your service credentials 
      - <service-instance-name>      same value as the instance name you created in previous step

      From the output of the command, 

      a. Copy the apikey value for later use

      b. Copy the kafka_brokers_sasl value for later use

      c. Edit the copied kafka_brokers_sasl value removing any single/double quotes and spaces, and add a comma separating each broker


## Using the Event Streams plugin

   Once you have created your service instance either using the CLI or UI, you can use the Event Streams plugin to manage your topics

   1. Install the CLI plug-in:

      `ibmcloud plugin install event-streams`

   2. Once you install the plug-in, you must initialize the the plug-in:

      `ibmcloud es init`

   3. To create topic:

      `ibmcloud es topic-create [--name] TOPIC_NAME [--partitions PARTITIONS]`

      `--name` being then name of your topic, `--partitions` being the number of partitions for your topic

   4. To delete topic:

      `ibmcloud es topic-delete [--name] TOPIC_NAME`

   5. To display details of a topic:

      `ibmcloud es topic [--name] TOPIC_NAME [--json]`

      `--json` is an optional parameter, it display the output of the command in JSON format
