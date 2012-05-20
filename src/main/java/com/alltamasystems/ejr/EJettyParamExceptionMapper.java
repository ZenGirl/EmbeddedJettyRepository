package com.alltamasystems.ejr;

import com.alltamasystems.ejr.utils.StackDump;
import com.sun.jersey.api.ParamException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Created with IntelliJ IDEA.
 * User: kim
 * Date: 20/05/12
 * Time: 10:00 AM
 * To change this template use File | Settings | File Templates.
 */
@Provider
public class EJettyParamExceptionMapper implements ExceptionMapper<ParamException> {

  private static final Logger log = LoggerFactory.getLogger(EJettyParamExceptionMapper.class);

  public Response toResponse(ParamException exception) {
    log.error("Invalid path accessed: " + exception.getMessage());
    log.error(StackDump.getStackTrace(exception));
    return Response.status(Response.Status.NOT_FOUND).entity("That path does not exist").build();
  }
}
