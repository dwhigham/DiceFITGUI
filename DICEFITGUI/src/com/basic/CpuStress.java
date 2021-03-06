package com.basic;

import com.jcraft.jsch.*;

import java.io.*;
import java.util.concurrent.TimeUnit;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

import org.primefaces.context.RequestContext;
import org.primefaces.model.UploadedFile;

@ManagedBean

public class CpuStress {
	public static String text = "cputest";
	
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
	
	public void stresscpu(String cores, String time, String vmpassword,
			String host, String sshkeypath, String output) {

		Globals.text= Globals.text + "STARTED";
		RequestContext requestContext = RequestContext.getCurrentInstance();
		requestContext.update(output);
		//Calls OS checker to determine if Ubuntu or Centos os
		OSchecker oscheck = new OSchecker();
		String localOS;
		try{
		oscheck.oscheck(host, vmpassword, sshkeypath);
		 localOS = oscheck.OSVERSION;
		}
		finally{
		
		
		}
		//LoggerWrapper.myLogger.info(localOS);	
		Globals.text= Globals.text + localOS;
		RequestContext requestContext1 = RequestContext.getCurrentInstance();
		requestContext1.update(output);
		//Set up for First command sent
		String command;
		if(localOS == null)
		{					
			Globals.text= Globals.text + " Unable to connect to VM";
			RequestContext requestContextexit = RequestContext.getCurrentInstance();
			requestContextexit.update(output);
			 return;
		}
		if (localOS.equals("UBUNTU"))
		{
		command ="dpkg-query -W -f='${Status}' stress";

		}
		else
		{
			//CENTOS will not accept first command so "dud" command sent
		command ="";
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		System.setOut(new PrintStream(baos));
		
		try {
			
			String info = null;

			JSch jsch = new JSch();

			String user = host.substring(0, host.indexOf('@'));
			host = host.substring(host.indexOf('@') + 1);
			Session session = jsch.getSession(user, host, 22);
			 //Used to determine if ssh key or password is proivded with command 
			if (sshkeypath.equals("-no")) {
				 session.setPassword(vmpassword);
			}
			  else if (vmpassword.equals("-no"))
			  {
					 jsch.addIdentity(sshkeypath);
			  }

			java.util.Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);
			session.connect();
			//test.concat("Attempting to SSH to VM with ip " + host);
			//LoggerWrapper.myLogger.info("Attempting to SSH to VM with ip " + host);
			Globals.text= Globals.text + host;
			RequestContext requestContext2 = RequestContext.getCurrentInstance();
			requestContext2.update(output);
			//Opens channel for sending first command.
			Channel channel = session.openChannel("exec");
			((ChannelExec) channel).setCommand(command);
			channel.setInputStream(null);

			((ChannelExec) channel).setErrStream(System.err);

			InputStream in = channel.getInputStream();

			channel.connect();

			byte[] tmp = new byte[1024];
			while (true) {
				while (in.available() > 0) {
					int i = in.read(tmp, 0, 1024);
					if (i < 0)
						break;
					System.out.print(new String(tmp, 0, i));
					info = new String(tmp, 0, i);
					//Outputs responce for ssh connection
					System.out.print(" Stress Status : " + info);
					Globals.text= Globals.text + info;
					RequestContext requestContext3 = RequestContext.getCurrentInstance();
					requestContext3.update(output);
				}
				if (channel.isClosed()) {
					if (in.available() > 0)
						continue;
					System.out.println("exit-status: "
							+ channel.getExitStatus());
					break;
				}
				try {
					Thread.sleep(1000);
				} catch (Exception ee) {
				}

			}
			//Closes after first command is sent
			channel.disconnect();
			//Sets up for second command
			String command2 = null;
			//Different commands used if Centos or Ubuntu OS is used.
			if  (localOS.equals("CENTOS"))
			{
				command2 = "wget http://dl.fedoraproject.org/pub/epel/6/x86_64/stress-1.0.4-4.el6.x86_64.rpm && rpm -ivh stress-1.0.4-4.el6.x86_64.rpm; stress -c " + cores + " -t " + time;
				//LoggerWrapper.myLogger.info("Installing Stress tool if required and running test..... ");
				Globals.text= Globals.text + " Installing Stress tool if required and running test..... ";
				}
			
			else if  (localOS.equals("UBUNTU"))
			{
				if (info == null) {
					
					command2 = "sudo apt-get -q -y install stress; stress -c " + cores + " -t " + time;
					//LoggerWrapper.myLogger.info("Stress tool not found..Installing...... The running test");
					Globals.text= Globals.text + " Stress tool not found..Installing...... The running test";
					RequestContext requestContext4 = RequestContext.getCurrentInstance();
					requestContext4.update(output);
				}
				else if (info.equals("install ok installed")) {
					command2 = "stress -c " + cores + " -t " + time;
					//LoggerWrapper.myLogger.info("Stress tool found..running test......");
					Globals.text= Globals.text + " Stress tool not found..Installing...... The running test";
					RequestContext requestContext4 = RequestContext.getCurrentInstance();
					requestContext4.update(output);
				}
			}

			Channel channel2 = session.openChannel("exec");
			((ChannelExec) channel2).setCommand(command2);
			InputStream in1 = channel2.getInputStream();
			channel2.connect();
			while (true) {
				while (in1.available() > 0) {
					int i = in1.read(tmp, 0, 1024);
					if (i < 0)
						break;
					System.out.print(new String(tmp, 0, i));
					info = new String(tmp, 0, i);
					System.out.print(info);
					Globals.text= Globals.text + info;
				}
				if (channel2.isClosed()) {
					if (in.available() > 0)
						continue;
					System.out.println("exit-status: "
							+ channel2.getExitStatus());
					Globals.text= Globals.text + channel2.getExitStatus();
					RequestContext requestContext5 = RequestContext.getCurrentInstance();
					requestContext5.update(output);
					break;
				}
				try {
					Thread.sleep(1000);
				} catch (Exception ee) {
				}

			}
			in1.close();
			//Close after second command
			channel2.disconnect();
			//Close session after all commands are done
			session.disconnect();
			//LoggerWrapper.myLogger.info( baos.toString());
			Globals.text= text +baos.toString();
			RequestContext requestContext6 = RequestContext.getCurrentInstance();
			requestContext6.update(output);
			
			Globals.text= Globals.text +" completed";
			RequestContext requestContext7 = RequestContext.getCurrentInstance();
			requestContext7.update(output);

		} catch (Exception e) {
			//LoggerWrapper.myLogger.severe("Unable to SSH to VM " + e.toString());
			Globals.text= text + "Unable to SSH to VM " + e.toString();
			RequestContext requestContext7 = RequestContext.getCurrentInstance();
			requestContext7.update(output);

		}
	}
}