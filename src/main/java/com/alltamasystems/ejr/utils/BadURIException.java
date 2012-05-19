package com.alltamasystems.ejr.utils;

import com.sun.jersey.api.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Created with IntelliJ IDEA.
 * User: kim
 * Date: 20/05/12
 * Time: 8:41 AM
 * To change this template use File | Settings | File Templates.
 */
@Provider
public class BadURIException implements ExceptionMapper<NotFoundException> {

  // Logger
  private static final Logger log = LoggerFactory.getLogger(BadURIException.class);
  /**
   * The actual override.
   *
   * @param exception passed in by the JAX-RS implementation.
   * @return a WebResponse passed to client.
   */
  public Response toResponse(NotFoundException exception) {
    log.error("Invalid path accessed: " + exception.getMessage());
    log.error(StackDump.getStackTrace(exception));
    return Response.status(Response.Status.NOT_FOUND).entity("{\"status\":false,\"messages\":\"9999:That path does not exist\",\"value\":[]}").build();
  }
}
