module Hust_Project1_PackageManager {
	requires javafx.controls;
	requires javafx.fxml;
	requires javafx.graphics;
	requires java.sql;
	requires org.apache.httpcomponents.httpclient;
	requires java.net.http;
	requires org.apache.httpcomponents.httpcore;
	requires java.xml;
	requires java.desktop;
	requires org.antlr.antlr4.runtime;
	requires json.simple;
	requires plexus.utils;
	requires maven.model;
	
	opens view;
	opens controller;
	opens model;
}
