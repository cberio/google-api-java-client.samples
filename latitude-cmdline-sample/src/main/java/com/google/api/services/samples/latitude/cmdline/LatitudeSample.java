/*
 * Copyright (c) 2010 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.api.services.samples.latitude.cmdline;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.java6.auth.oauth2.FileCredentialStore;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.http.json.JsonHttpRequest;
import com.google.api.client.http.json.JsonHttpRequestInitializer;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.latitude.Latitude;
import com.google.api.services.latitude.LatitudeRequest;
import com.google.api.services.latitude.LatitudeScopes;
import com.google.api.services.latitude.model.Location;
import com.google.api.services.latitude.model.LocationFeed;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

/**
 * @author Yaniv Inbar
 */
public class LatitudeSample {

  /** Global instance of the HTTP transport. */
  private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

  /** Global instance of the JSON factory. */
  private static final JsonFactory JSON_FACTORY = new JacksonFactory();

  /** Authorizes the installed application to access user's protected data. */
  private static Credential authorize() throws Exception {
    // load client secrets
    GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
        JSON_FACTORY, LatitudeSample.class.getResourceAsStream("/client_secrets.json"));
    if (clientSecrets.getDetails().getClientId().startsWith("Enter")
        || clientSecrets.getDetails().getClientSecret().startsWith("Enter ")) {
      System.out.println(
          "Enter Client ID and Secret from https://code.google.com/apis/console/?api=latitude "
          + "into latitude-cmdline-sample/src/main/resources/client_secrets.json");
      System.exit(1);
    }
    // set up file credential store
    FileCredentialStore credentialStore = new FileCredentialStore(
        new File(System.getProperty("user.home"), ".credentials/latitude.json"), JSON_FACTORY);
    // set up authorization code flow
    GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
        HTTP_TRANSPORT, JSON_FACTORY, clientSecrets,
        Collections.singleton(LatitudeScopes.LATITUDE_ALL_BEST)).setCredentialStore(credentialStore)
        .build();
    // authorize
    return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
  }

  private static void run() throws Exception {
    // authorization
    Credential credential = authorize();

    // set up Latitude
    Latitude latitude = new Latitude.Builder(
        HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName("Google-LatitudeSample/1.0")
        .setJsonHttpRequestInitializer(new JsonHttpRequestInitializer() {
            @Override
          public void initialize(JsonHttpRequest request) {
            LatitudeRequest latitudeRequest = (LatitudeRequest) request;
            latitudeRequest.setPrettyPrint(true);
          }
        }).build();

    showCurrentLocation(latitude);
    showLocationHistory(latitude);
  }

  public static void main(String[] args) {
    try {
      try {
        run();
        // success!
        return;
      } catch (IOException e) {
        System.err.println(e.getMessage());
      }
    } catch (Throwable t) {
      t.printStackTrace();
    }
    System.exit(1);
  }

  private static void showCurrentLocation(Latitude latitude) throws IOException {
    Latitude.CurrentLocation.Get request = latitude.currentLocation().get();
    // (optional) The finest granularity of locations you want to access. Can
    // be either city or best. If this parameter is omitted, city is assumed.
    request.setGranularity("best");
    Location currentLocation = request.execute();
    System.out.println("Current location:");
    System.out.println(currentLocation);
  }

  private static void showLocationHistory(Latitude latitude) throws IOException {
    Latitude.Location.List request = latitude.location().list();
    // (optional) The finest granularity of locations you want to access. Can
    // be either city or best. If this parameter is omitted, city is assumed.
    request.setGranularity("best");
    LocationFeed locationFeed = request.execute();
    System.out.println("Location History:");
    if (locationFeed.getItems() == null) {
      System.out.println("No location history found.");
    } else {
      for (Location location : locationFeed.getItems()) {
        System.out.println(location);
      }
    }
  }
}
