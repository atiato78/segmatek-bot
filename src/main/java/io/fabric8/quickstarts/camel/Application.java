/*
 * Copyright 2005-2016 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version
 * 2.0 (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */
package io.fabric8.quickstarts.camel;
import java.util.HashMap;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import javax.activation.DataHandler;
import javax.mail.BodyPart;
import javax.mail.Header;
import javax.mail.internet.ContentDisposition;
import javax.mail.internet.MimeBodyPart;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import javax.sql.DataSource;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.component.servlet.CamelHttpTransportServlet;
import org.apache.camel.http.common.HttpMessage;
import org.apache.camel.http.common.HttpOperationFailedException;
import org.apache.camel.impl.DefaultAttachment;
import org.apache.camel.impl.SimpleRegistry;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.model.rest.RestParamType;
import org.apache.camel.spi.Registry;
import org.apache.camel.spring.spi.SpringTransactionPolicy;
import org.apache.camel.util.jsse.KeyManagersParameters;
import org.apache.camel.util.jsse.KeyStoreParameters;
import org.apache.camel.util.jsse.SSLContextParameters;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.twilio.Twilio;



import org.apache.camel.util.jsse.SSLContextParameters;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

@SpringBootApplication
@ImportResource({ "classpath:spring/camel-context.xml" })
public class Application extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);

    }

    Map<String, String> myHashTable = new HashMap<String, String>();

    @Bean
    ServletRegistrationBean servletRegistrationBean() {
        ServletRegistrationBean servlet = new ServletRegistrationBean(new CamelHttpTransportServlet(),
                "/camel-rest-sql/*");
        servlet.setName("CamelServlet");
        return servlet;
    }

   

    @Component
    class RestApi extends RouteBuilder {

        @Override
        public void configure() {

            // onException(HttpOperationFailedException.class).handled(true).process(new
            // Processor() {
            // @Override
            // public void process(Exchange exchange) throws Exception {
            // // exchange.getIn().setBody("{Exception occured :"+ex.getMessage()+"}");
            // UnAuthorize test = new UnAuthorize();
            // test.setResult("You are UnAuthrized to access such API");
            // exchange.getIn().setBody(test);
            // }
            // });

            restConfiguration()

                    .contextPath("/camel-rest-sql").apiContextPath("/api-doc")
                    .apiProperty("api.title", "Camel REST API").apiProperty("api.version", "1.0")
                    .component("servlet").bindingMode(RestBindingMode.json);

            rest("/").description("Exhange Rate REST service").post("whatsapp/")
                    .description("The drools for specified currency").route().routeId("drools-rate-api")
                    .log("${header.Body}").to("log:DEBUG?showBody=true&showHeaders=true")
                    .removeHeaders("Camel*")
                    .streamCaching()
                    .setHeader(Exchange.HTTP_METHOD, constant(org.apache.camel.component.http.HttpMethods.POST))
               //     .setHeader(Exchange.HTTP_QUERY,simple("format=json&ticket_id=${header.Body}"))
               .process(new Processor() {
                public void process(Exchange exchange) throws Exception {

                    String body= new String("{\"seq\": \"1234567890\",\"action\": \"service.seq\",\"uid\": \"987654321\",\"acceptno\": \"987654321\",\"nickname\": \"Test User\",\"businessid\": \"4\",\"accesspwd\": \"segmatec-channel-password\",\"tenantid\": \"999999999\",\"timestamp\": \"20200808080808\"}");

                      String tokenid = myHashTable.get(exchange.getIn().getHeader("From"));

                      exchange.getIn().setHeader("tokenid",tokenid);
                      
                      log.info("From "+exchange.getIn().getHeader("From").toString());
                      exchange.getIn().setHeader("FromWhats",exchange.getIn().getHeader("From").toString());


                      exchange.getIn().setBody(body);



                    }

                })
                .choice()
                .when(simple("${header.tokenid} == null"))
                
                
                    .setHeader(Exchange.HTTP_URI, constant("https://csp.crm.huawei.com/csp/mca/magentChannelAccess/adapter"))
                    .setHeader("CamelHttpUrl",constant("https://csp.crm.huawei.com/csp/mca/magentChannelAccess/adapter"))
               
                    .setHeader("Accept", constant("application/json"))
                    .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
                    .to("log:DEBUG?showBody=true&showHeaders=true")
                    .to("https://csp.crm.huawei.com/csp/mca/magentChannelAccess/adapter")
                    .log("${body}")
                    .convertBodyTo(String.class)
                    .process(new Processor() {
                        public void process(Exchange exchange) throws Exception {
     

                            String output =   exchange.getIn().getBody(String.class);
                            log.info("well "+exchange.getIn().getBody(String.class));
                            JSONObject obj=new JSONObject(output);
                            String tokenid =obj.getString("tokenid");
                            log.info("tokenid "+tokenid);
                            exchange.getIn().setHeader("tokenid",tokenid);


                            log.info("From "+exchange.getIn().getHeader("FromWhats").toString());
                            // "\n token"+exchange.getIn().getHeader("token").toString());


                            myHashTable.put(exchange.getIn().getHeader("FromWhats").toString(),exchange.getIn().getHeader("tokenid").toString());

                            log.info(myHashTable.get(exchange.getIn().getHeader("From"))); 
                            
                            for (String key : myHashTable.keySet()) {
                                log.info("I'm printing key "+key);
                            }
                
                            for (String value : myHashTable.values()) {
                                log.info("I'm printing value "+value);
                            }
                             Boolean test = true;
                             String created_time = null;
                             String status = null;
                             String status_reason = null;
                             String ticket_handler = null;
                             String note = null;

                          

                        //     JSONObject data = json.getJSONObject("data");

                        //     String totalBalance=data.getString("totalBalance");
                        //     String totalUnusedMinutes = data.getString("totalUnusedMinutes");
                        //     String totalUnusedGigabytes = data.getString("totalUnusedGigabytes");
                        //     String totalUnusedSMSs = data.getString("totalUnusedSMSs");
                        //     String totalUnusedUnits = data.getString("totalUnusedUnits");

                        //     String freeunits="<?xml version=\"1.0\" encoding=\"UTF-8\" ?> <Response><Message>You have total Balance "+totalBalance+"\n"+"Total Unused Mintues "+totalUnusedMinutes+
                        //     "\nTotal Unused Data "+totalUnusedGigabytes+"\nTotal Unused SMS "+totalUnusedSMSs+"\n</Message></Response>";

                            
                            
                            
                        //    JSONArray accounts= data.getJSONArray("accounts");
                        //    String resultMessage=null;
                        //    StringBuilder sb = new StringBuilder();
                        //    sb.append("<Message>Your Available Services :\n");
                        //    for (int i=0 ; i <accounts.length();i++)

                        //    {

                        //     log.info("services"+accounts.getJSONObject(i).getString("name"));
                        //     int t=i+1;

                        //     resultMessage =t+"-"+accounts.getJSONObject(i).getString("name")+"\n";
                        //     sb.append(resultMessage);
                        //     log.info(sb.toString());

                        //    }
                        //    sb.append("Please Send *Details* to get Balance and Free Units</Message>");


                       

                           
                            
                        
                //  exchange.getIn().setHeader(Exchange.CONTENT_TYPE, MediaType.APPLICATION_XML);
                
                //  String result = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?> <Response><Message>*Ticket Status:* "+status+"\n*Status Reason:* "+status_reason+"\n*Handler:* "+ticket_handler+"\n*Note:* "+note+"\n*Created Time:* "+created_time+"</Message></Response>";

                //  String error = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?> <Response><Message>Welcome to Segmatek.\nTicket Not Found.\nPlease Send *Ticket_ID* to Query Ticket Status.\nEnjoy Our Service\n"+"</Message></Response>";
 
                //  if (test)
                //              exchange.getIn().setBody(result);
                //  //             else if (exchange.getIn().getHeader("Body").toString().equals("Details"))
                //  //             exchange.getIn().setBody(freeunits);
                //               else 
                //              exchange.getIn().setBody(error);

                            

                          

                        }
                    })
                    .end()
                    .process(new Processor() {
                        public void process(Exchange exchange) throws Exception {
                    
                            String request = new String ("{\"seq\": \"1234567890\",\"action\": \"message.send\",\"content\": \""+exchange.getIn().getHeader("Body")+"\",\"contenttype\": \"0\",\"msgtype\": \"0\",\"extinfo\": null,\"timestamp\": \"20200808080808\"}");


                            exchange.getIn().setBody(request);

                    
                        }
                    
                    
                    })
                    .setHeader(Exchange.HTTP_URI, constant("https://csp.crm.huawei.com/csp/mca/magentChannelAccess/adapter"))
                    .setHeader("CamelHttpUrl",constant("https://csp.crm.huawei.com/csp/mca/magentChannelAccess/adapter"))
               
                    .setHeader("Accept", constant("application/json"))
                    .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
                    .to("log:DEBUG?showBody=true&showHeaders=true")
                    .to("https://csp.crm.huawei.com/csp/mca/magentChannelAccess/adapter")
                    .log("${body}")
                    .convertBodyTo(String.class)
                    
                    .delay(30000)

                    .process(new Processor() {
                        public void process(Exchange exchange) throws Exception {
                    
                            String request = new String ("{\"seq\": \"1234567890\",\"action\": \"message.pull\",\"timestamp\": \"20200808080808\"}");


                            exchange.getIn().setBody(request);

                    
                        }
                    
                    
                    })

                    .setHeader(Exchange.HTTP_URI, constant("https://csp.crm.huawei.com/csp/mca/magentChannelAccess/adapter"))
                    .setHeader("CamelHttpUrl",constant("https://csp.crm.huawei.com/csp/mca/magentChannelAccess/adapter"))
               
                    .setHeader("Accept", constant("application/json"))
                    .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
                    .to("log:DEBUG?showBody=true&showHeaders=true")
                    .to("https://csp.crm.huawei.com/csp/mca/magentChannelAccess/adapter")
                    .log("${body}")
                    .convertBodyTo(String.class)
                    .process(new Processor() {
                        public void process(Exchange exchange) throws Exception {


                            String output =   exchange.getIn().getBody(String.class);
                            log.info("well "+exchange.getIn().getBody(String.class));
                            JSONObject obj=new JSONObject(output);
                        //    String result =obj.getString("msglist");
                            JSONArray arr=obj.getJSONArray("msglist");

                      //      JSONArray test = new JSONArray(arr);


                            String result = null;
                            StringBuilder outcome = new StringBuilder();

                            for (int i=0 ;i<arr.length();i++)

                            {
                             log.info(arr.getJSONObject(i).getString("content"));
                             result = "<Message>"+arr.getJSONObject(i).getString("content")+"</Message>";
                             outcome.append(result);
                            }




                    
                               exchange.getIn().setHeader(Exchange.CONTENT_TYPE, MediaType.APPLICATION_XML);
                
                 String result1 = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?> <Response>"+outcome+"</Response>";

               ///  String error = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?> <Response><Message>Welcome to Segmatek.\nTicket Not Found.\nPlease Send *Ticket_ID* to Query Ticket Status.\nEnjoy Our Service\n"+"</Message></Response>";
 
                //  if (test)
                              exchange.getIn().setBody(result1);
                //  //             else if (exchange.getIn().getHeader("Body").toString().equals("Details"))
                //  //             exchange.getIn().setBody(freeunits);
                //               else 
                //              exchange.getIn().setBody(error);

                    
                        }
                    
                    
                    })



                    
                    
                    
                    .endRest();

        }

    }

    
}
