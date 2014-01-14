/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.harvard.iq.dataverse.api;


import edu.harvard.iq.dataverse.SearchServiceBean;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.ejb.EJB;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.Produces;

/*
    Custom API exceptions [NOT YET IMPLEMENTED]
import edu.harvard.iq.dataverse.api.exceptions.NotFoundException;
import edu.harvard.iq.dataverse.api.exceptions.ServiceUnavailableException;
import edu.harvard.iq.dataverse.api.exceptions.PermissionDeniedException;
import edu.harvard.iq.dataverse.api.exceptions.AuthorizationRequiredException;
*/

/**
 *
 * @author Leonid Andreev
 * 
 * The metadata access API is based on the DVN metadata API v.1.0 (that came 
 * with the v.3.* of the DVN app) and extended for DVN 4.0 to include more
 * granular access to subsets of the metatada that describe the dataaset: 
 * access to individual datafile and datavariable sections, as well as  
 * specific fragments of these sections. 
 */

@Path("meta")
public class Meta {
    private static final Logger logger = Logger.getLogger(Meta.class.getCanonicalName());

    @EJB
    SearchServiceBean searchService;

    @Path("variable/{varId}")
    @GET
    @Produces({ "application/xml" })

    public String variable(@PathParam("varId") Long varId, @QueryParam("partialExclude") String partialExclude, @QueryParam("partialInclude") String partialInclude) /*throws NotFoundException, ServiceUnavailableException, PermissionDeniedException, AuthorizationRequiredException*/ {
        String retValue = "";
        
        
        return retValue; 
    }
    
    @Path("datafile/{fileId}")
    @GET
    @Produces({ "application/xml" })
    public String datafile(@PathParam("fileId") Long fileId, @QueryParam("partialExclude") String partialExclude, @QueryParam("partialInclude") String partialInclude) /*throws NotFoundException, ServiceUnavailableException, PermissionDeniedException, AuthorizationRequiredException*/ {
        String retValue = "";
        
        
        return retValue; 
    }
    
}