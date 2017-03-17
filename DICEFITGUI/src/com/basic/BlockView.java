package com.basic;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Date;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import org.apache.commons.io.FilenameUtils;
import org.primefaces.model.UploadedFile;
 
@ManagedBean
public class BlockView {
     
    private String ip;
    private String username;
    private String password;
	private String sshkeypath;
	private UploadedFile file;
	private String test = "";
	
    public String getIp() {
        return ip;
    }
    public String getTest() {
        return test;
    }
    public void setIp(String ip) {
        this.ip = ip;
    }
 
    public String getUsername() {
        return username;
    }
 
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
 
    public void setPassword(String password) {
        this.password = password;
    }
 
	public UploadedFile getFile() {
	        return file;
	    }
	 
	public void setFile(UploadedFile file) {
	        this.file = file;
	    }
	
	public String getSshkeypath() {
		return sshkeypath;
	}
	public void setSshkeypath(String sshkeypath) {
		this.sshkeypath = sshkeypath;
	}
	     
	    private static final File LOCATION = new File("/Users/darrenw/Downloads/PrimeTest/Uploads");

	 public void upload() throws IOException {
	        
	    	try (InputStream input = file.getInputstream()) {
	            Date date = new Date();
	            long datetime = date.getTime();

	    		//Files.copy(input, new File("/Users/darrenw/Downloads/PrimeTest/Uploads/"+ datetime + file.getFileName()).toPath())
	    	  //  File fileper = new File("/Users/darrenw/Downloads/PrimeTest/Uploads/"+ datetime + file.getFileName());
	            Files.copy(input, new File("/home/ubuntu/Uploads/"+ datetime + file.getFileName()).toPath());
	    	    File fileper = new File("/home/ubuntu/Uploads/"+ datetime + file.getFileName());
	    	    fileper.setReadable(true, false);
	    	    fileper.setWritable(true, false);
	    	  //  setSshkeypath("/Users/darrenw/Downloads/PrimeTest/Uploads/"+ datetime + file.getFileName());
		    	  setSshkeypath("/home/ubuntu/Uploads/"+ datetime + file.getFileName());

	    	    System.out.println(sshkeypath.toString());
	    	}     
	    }
	    
	    public void message()
	    {
	    	FacesContext.getCurrentInstance().addMessage(null,
	                new FacesMessage("Fault Started : " + ip + " " + username));
	    	save();
	    }
	    
    public void save(){
    	
    	if (getPassword().equals("-no"))
   	 {				
    	 		password= "-no";
    	 		String host= username +"@"+ip;
    	 	    BlockVM blockvm = new BlockVM();
    	 	   blockvm.blockfirewall(host, password, getSshkeypath());
    	 		//LoggerWrapper.myLogger.info( "Executing CPU stress on VM with sshkey");	
    	 	  File file = new File(getSshkeypath());
    	 	  file.delete();
   	 }
    	 	else
    	 	{
    	 		sshkeypath = "-no";
    	  	    String host= username +"@"+ip;
    	 	    BlockVM blockvm = new BlockVM();
    	 	   blockvm.blockfirewall(host, password, sshkeypath);
    	 	    //LoggerWrapper.myLogger.info( "Executing CPU stress on VM with no sshkey");
    	 	}
    }
}