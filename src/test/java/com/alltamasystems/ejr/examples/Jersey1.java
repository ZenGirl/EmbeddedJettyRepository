package com.alltamasystems.ejr.examples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

// NOTE! We HAVE to add an extra context here, so in this case the url would be
// http://localhost:9301/resources1/user1
@Path("/user1")
public class Jersey1 {

  private static Logger log = LoggerFactory.getLogger(Jersey1.class);

  public Jersey1() {
    log.warn("Instance of Jersey1 created");
  }

  // You can add paths from here on...
  @GET
  @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
  public ExamplePojo getUser() {
    log.warn("Inside ExampleJerseyResource1 getUser()");
    ExamplePojo pojo = new ExamplePojo();
    pojo.setNumber(100);
    pojo.setWords("hello world 1");
    return pojo;
  }
  
  @GET
  @Path("{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public ExamplePojo getAnotherUser(@PathParam("id") int id) {
    log.warn("Inside ExampleJerseyResource1 getAnotherUser(" + id + ")");
    ExamplePojo pojo = new ExamplePojo();
    pojo.setNumber(id);
    pojo.setWords("hello world 1");
    return pojo;
  }
}
