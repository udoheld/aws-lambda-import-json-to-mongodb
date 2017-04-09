/*
    Copyright 2017 the original author or authors.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with this program. If not, see
    <http://www.gnu.org/licenses/>.
 */

package com.udoheld.aws.lambda.json.to.mongodb;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Udo Held
 */
public class TestSnsMessageExtractor {

  @Test
  public void testSnsMessageExtraction(){
    String input = "{ \"Records\": [ { \"EventSource\": \"aws:sns\", \"EventVersion\": \"1.0\", \"EventSubscriptionArn\": \"arn:aws:sns:ap-southeast-2:858689980767:iot-ingress:9ee12977-01a0-4a3e-a606-a44f547e2d95\", \"Sns\": { \"Type\": \"Notification\", \"MessageId\": \"01ba0176-7c9f-50dc-9feb-5014b445839c\", \"TopicArn\": \"arn:aws:sns:ap-southeast-2:858689980767:iot-ingress\", \"Subject\": null, \"Message\": \"{\\\"d\\\":[{\\\"bn\\\":\\\"urn:dev:mac:784b87a58c3d;temp1\\\",\\\"bt\\\":1491650201.048,\\\"n\\\":\\\"temp\\\",\\\"u\\\":\\\"Cel\\\",\\\"v\\\":27.1}],\\\"clientid\\\":\\\"edison-1\\\",\\\"timestamp\\\":1491650201148,\\\"topic\\\":\\\"iot/sensordata/prod/json\\\"}\", \"Timestamp\": \"2017-04-08T11:16:42.226Z\", \"SignatureVersion\": \"1\", \"Signature\": \"E7JNsOUB6fnTQ558U+WE/z9XlXIMCPPML+s13ekvt7LaD0NZ5xeNHMi5O2rgc0/VXEWbDq+UaoODt8aM4GMyGh3S7ZiyyTlYVqtWSRhr+hWElX+zBnLcyaj+d3fUELCf8iY9Ng0Sw5RcfgAdiYBsr+mGDtIQX9LgQ2xHThOlMDtcBwpQMt/4roLyo+xQVSIkcawNEiehbCnSGBpdQuEZYF3Z3KST/Di9VeVVOYjGyFlDgWTinL8Q0UW0aqRkfGdtk6bBflPnnu+X58XOmcuHua16U2F3S8sx9IJAMr1OdhghF5I/JMWzCje8fahBTXdWyXwi3t4lWbaJRmOY1emCBw==\", \"SigningCertUrl\": \"https://sns.ap-southeast-2.amazonaws.com/SimpleNotificationService-b95095beb82e8f6a046b3aafc7f4149a.pem\", \"UnsubscribeUrl\": \"https://sns.ap-southeast-2.amazonaws.com/?Action=Unsubscribe&SubscriptionArn=arn:aws:sns:ap-southeast-2:858689980767:iot-ingress:9ee12977-01a0-4a3e-a606-a44f547e2d95\", \"MessageAttributes\": {} } } ]}";
    String expectedOutput = "{\"d\":[{\"bn\":\"urn:dev:mac:784b87a58c3d;temp1\",\"bt\":1491650201.048,\"n\":\"temp\",\"u\":\"Cel\",\"v\":27.1}],\"clientid\":\"edison-1\",\"timestamp\":1491650201148,\"topic\":\"iot/sensordata/prod/json\"}";

    String output = SnsMessageExtractor.extractSnsMessage(input);
    assertNotNull(expectedOutput);
    assertEquals(expectedOutput, output);
  }
}
