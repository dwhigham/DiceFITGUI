package com.basic;

import com.jcraft.jsch.*;

import java.io.*;

import javax.faces.bean.ManagedBean;

import org.primefaces.context.RequestContext;
@ManagedBean
public class OSchecker {
	
	public String OSVERSION;
	public static String text = "";
	
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
	public void oscheck(String host, String vmpassword,String sshkeypath) {
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
			//LoggerWrapper.myLogger.info("Attempting to SSH to VM for OS check with ip " + host);
			// use command to check if ubuntu is OS
			String command ="grep 'NAME=\"Ubuntu\"' /etc/*-release";
					

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
					//Output OS version to Log
					System.out.print("OS version : " + info);
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
			
			//If Ubuntu is not found them assume its Centos
			 if (info == null) {
				 OSVERSION = "CENTOS";	
				//LoggerWrapper.myLogger.info("CENTOS OS");
			}
			 else if (info.contains("Ubuntu")) {
					OSVERSION = "UBUNTU";	
					//LoggerWrapper.myLogger.info("UBUNTU OS");
					
				}
			 

			in.close();
			//Close after command sent
			channel.disconnect();
			//Close session after all commands are done
			session.disconnect();
			//LoggerWrapper.myLogger.info( baos.toString());

		}catch (Exception e) {
			//LoggerWrapper.myLogger.severe("Unable to SSH to VM " + e.toString());
			Globals.text= text + sshkeypath + "VALUE:";
			Globals.text= text + "Unable to SSH to VM " + e.toString();
			RequestContext requestContext7 = RequestContext.getCurrentInstance();
		}
	}
}