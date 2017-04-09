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

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

/**
 * Extracts the actual message, if the content is passed from SNS and contains SNS headers.
 * @author Udo Held
 */
public class SnsMessageExtractor {

  /**
   * Extract the message from the SNS header.
   * @param input Json including SNS header
   * @return The message from the first record, if found. Otherwise null.
   */
  public static String extractSnsMessage(String input) {
    Gson gson = new Gson();
    SnsMessage message = gson.fromJson(input, SnsMessage.class);
    if (message != null && message.getRecords() != null && message.getRecords().length > 0
        && message.getRecords()[0].getSns() != null) {
      return message.getRecords()[0].getSns().getMessage();
    }
    return null;
  }

  /**
   * Top level structure for SNS json.
   */
  public static class SnsMessage {
    @SerializedName("Records")
    private SnsRecord [] records;

    public SnsRecord[] getRecords() {
      return records;
    }

    public void setRecords(SnsRecord[] records) {
      this.records = records;
    }
  }

  public static class SnsRecord {
    @SerializedName("Sns")
    private Sns sns;

    public Sns getSns() {
      return sns;
    }

    public void setSns(Sns sns) {
      this.sns = sns;
    }
  }

  public static class Sns {
    @SerializedName("Message")
    private String message;

    public String getMessage() {
      return message;
    }

    public void setMessage(String message) {
      this.message = message;
    }
  }

}
