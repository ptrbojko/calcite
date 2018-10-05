/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.calcite.chinook;

import org.apache.calcite.avatica.Meta;
import org.apache.calcite.avatica.jdbc.JdbcMeta;
import org.apache.calcite.avatica.remote.Driver;
import org.apache.calcite.avatica.remote.Service;
import org.apache.calcite.avatica.server.AvaticaProtobufHandler;
import org.apache.calcite.avatica.server.HttpServer;
import org.apache.calcite.avatica.server.Main;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Wrapping Calcite engine with Avatica tansport for testing JDBC capabilities between
 * Avatica JDBC transport and Calcite
 */
public class ChinookAvaticaServer {

  private HttpServer server;

  public void start() throws Exception {
    this.server = Main.start(new String[]{ChinookMetaFactory.class.getName()}, 0,
        new Main.HandlerFactory() {
          @Override public AvaticaProtobufHandler createHandler(Service service) {
            return new AvaticaProtobufHandler(service);
          }
        });
  }

  public String getURL() {
    return "jdbc:avatica:remote:url=http://localhost:" + server.getPort() + ";serialization="
        + Driver.Serialization.PROTOBUF.name();
  }

  public void stop() {
    server.stop();
  }

  /**
   * Factory for chinnok calcite database wrapped in meta for avatica
   */
  public static class ChinookMetaFactory implements Meta.Factory {

    private static CalciteConnectionProvider provider = new CalciteConnectionProvider();

    private static JdbcMeta instance = null;

    private static JdbcMeta getInstance() {
      if (instance == null) {
        try {
          instance = new JdbcMeta(provider.DRIVER_URL, provider.provideConnectionInfo());
        } catch (SQLException | IOException e) {
          throw new RuntimeException(e);
        }
      }
      return instance;
    }

    @Override public Meta create(List<String> args) {
      return getInstance();
    }
  }
}

// End ChinookAvaticaServer.java
