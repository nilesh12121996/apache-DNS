/*
* CS 656 / Fall 20 / Apache / V2.00
* Group: N7 / Leader: Nilesh (ns934)
* Group Members: Mushir (ms2944), Unnit (ugp2)
*
*   ADC - add your code here
*   NOC - do not change this
*/

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
// other imports go here
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;


/*--------------- end imports --------------*/

class ApacheTwo {

  // NOC these 3 fields
  private byte []     HOST ;      /* should be initialized to 1024 bytes in the constructor */
  private int         PORT ;      /* port this Apache should listen on, from cmdline        */
  private InetAddress PREFERRED ; /* must set this in dns() */
  // ADC additional fields here
  byte[] host= new byte[1024];
  byte[] extra= new byte[1024];
  private byte [] response = new byte[1100];
  private byte [] path = new byte[1100];
  byte[] url, reqb0;
  byte[] httpport=new byte[5];
  int totbyt=0, reqtype=-1;  //reqtype 0=DNS 1=HTTP 2=FTP
  private  byte[] FILE ;      /* name of the file in URL, if you like */

public static void main(String [] a) { // NOC - do not change main()

  ApacheTwo apache = new ApacheTwo(Integer.parseInt(a[0]));
  apache.run(2);

}

ApacheTwo(int port) {
  PORT = port;
  // other init stuff ADC here
  HOST= new byte[1024];
}

// Note: parse() must set HOST correctly
int parse(byte [] buffer)
{
	byte[] protocol=new byte[4];
	byte[] method=new byte[4];
	boolean pathexist=false;
	byte[] tmp = {'O','n','l','y',' ','G','E','T',' ','c','o','m','m','a','n','d',' ','i','s',' ','s','u','p','p','o','r','t','e','d','\r','\n'};
	byte[] tmp1 = {'O','n','l','y',' ','H','T','T','P',' ','a','n','d',' ','F','T','P',' ','a','r','e',' ','s','u','p','p','o','r','t','e','d','\n'};
	url=new byte[totbyt-5];
	HOST=new byte[1024];
	byte[] remainder=new byte[8];
	if(buffer[totbyt-11]==32)
		{for(int i=totbyt-10;i<totbyt-2;i++)
			remainder[i-(totbyt-10)]=buffer[i];}
	
	for (int i = 0; i < 4; i++)
			method[i]=buffer[i];
	byte[] temp3 = {'H','T','T','P','/','1','.','1'};
	if(!cmp(remainder,0,temp3.length,temp3) )
	  {reqtype=0;
	  for (int i=4;i<totbyt-2;i++)
		  HOST[i-4]=buffer[i];}
	else
		{for (int i=4;i<totbyt-10;i++)
			url[i-4]=buffer[i];
	int urllen=url.length;
	if(url[0]!=47)
	{for(int i=0;i<urllen;i++)
	{
		if(url[i]==58)
		{protocol=new byte[i];
		for(int j=0;j<i;j++)
			protocol[j]=url[j];
		for(int j=i+3;j<urllen;j++)
			{
			if(url[j]==58)
			{for(int k=j+1;k<urllen;k++)
				{
				if(url[k]==47)
					{j=k-1;break;}
				else if(url[k]==32)
				{j=k-1;break;}
				httpport[k-(j+1)]=url[k];
				}}
			else if(url[j]==47)
				{i=j;pathexist=true;break;}
			else
				HOST[j-(i+3)]=url[j];}
		if(pathexist)
		{path=new byte[urllen-i];
			for(int j=i;j<urllen;j++)
			{
				if(url[j]==32)
					{i=j+1;break;}
				else
					path[j-i]=url[j];
			}}
		else
			{path =new byte[1];path[0]='/';}
	   }}
	}}
	httpport=trim(httpport);
	byte[] tmp2= {'8','0'};
	if(httpport.length==0)
		httpport=tmp2;
	HOST=trim(HOST);
	path=trim(path);
	byte[] temp = {'G','E','T'};
	if(!cmp(method,0,temp.length,temp) )
		{response=tmp;return -1;}
	byte[] temp1 = {'H','T','T','P','/','1','.','1'};
	byte[] temp2 = {'f','t','p'};
	byte[] temp4 = {'h','t','t','p'};
	if(cmp(remainder,0,temp1.length,temp1) )
	{	if(url[0]==47 || cmp(protocol,0,temp2.length,temp4) )
			reqtype=1;
		else if(cmp(protocol,0,temp2.length,temp2) )
			reqtype=2;
		else
			{reqtype=-1;response=tmp1; return -1;}
		}
	return 0; }

// Note: dns() must set PREFERRED
int dns(int X)  // NOC - do not change this signature; X is whatever you want
{ byte[] tmp2 = {'E','R','R','O','R',' ','(','U','n','k','n','o','w','n','H','o','s','t',')','1','2','3','4','\n'};
	long strt,end;
	PREFERRED=null;
 long elapsed=9223372036854775807L;
	try {
		InetAddress[] tmp=InetAddress.getAllByName(byteToString(HOST, 0, HOST.length));
		int shortest=-1;
		for (int i = 0; i < tmp.length; i++) {
			strt = System.nanoTime();
				try(		Socket sct =new Socket()){
					sct.connect(new InetSocketAddress(tmp[i], 80), 5000);
			 end = System.nanoTime();
			 if(elapsed>(end-strt))
			 {elapsed=end-strt;shortest=i;}
				}catch(IOException e) {}}
		if(shortest>=0)
			PREFERRED=tmp[shortest];
		else
			PREFERRED=tmp[0];
		byte[] tmp3= {' ','(','P','R','E','F','E','R','R','E','D',')','\n'};
		response= mrg(PREFERRED.getHostAddress().getBytes(),tmp3);
	} catch (UnknownHostException e) {
		response= tmp2;return -1;
	}
	return 0; }

int http_fetch(Socket c) // NOC - don't change signature
{
	int totalbytes=0,tmpbytes=0;
	byte[] tmp3 = {'I','P',':',' ','E','R','R','O','R',' ','(','U','n','k','n','o','w','n','H','o','s','t',')','A','B','C','D','\n'};
	byte[] tmp4 = {'I','P',':',' ','E','R','R','O','R',' ','(','I','O',' ','E','r','r','o','r',')','\n'};
	byte[] tmp12= {'\r','\n'};
	byte[] tmp15= {'c','l','o','s','e'};
	byte[] tmp16= {'k','e','e','p','-','a','l','i','v','e'};
	try
    { 
		Socket p= new Socket(PREFERRED, AscbToInt(httpport, 0, httpport.length));   // peer, connection to HOST  fetch HTTP port
		InputStream httpin = p.getInputStream();
        OutputStream httpout    = p.getOutputStream();
        byte[] reqb1=replace(reqb0, 0, reqb0.length, tmp16, tmp15);
        httpout.write(reqb1);
        while(true)  // Read 65000bytes in each iteration and exit when tmpbytes returns -1 
	        {FILE=new byte[65535];
	        tmpbytes=httpin.read(FILE);
	        totalbytes+=tmpbytes;
	        if(tmpbytes>0)
	        	c.getOutputStream().write(FILE,0,tmpbytes);
	        else 
	        	{c.getOutputStream().write(tmp12);break;}}
        p.close();
    }
	catch (UnknownHostException e) {
		response= tmp3;
	} catch (IOException e) {
		System.out.println(e);
		response= tmp4;
	}
return totalbytes;
}

int  ftp_fetch(Socket c) // NOC - don't change signature
{
	int totalbytes=0,tmpbytes=0;
	byte[] ftpuser={'U','S','E','R',' ','a','n','o','n','y','m','o','u','s','\r','\n'};
	byte[] ftppass={'P','A','S','S',' ','a','n','o','n','y','m','o','u','s','\r','\n'};
	byte[] ftpapsv={'P','A','S','V','\r','\n'};
	byte[] ftpsz= {'S','I','Z','E',' '};
	byte[] ending= {'\r','\n'};
	byte[] ftpsize= mrg(mrg(ftpsz,path),ending);
	byte[] ftprtr= {'R','E','T','R',' '};
	byte[] ftpretr= mrg(mrg(ftprtr,path),ending);
	byte[] tmp5 = {'H','T','T','P','/','1','.','1',' ','4','0','0',' ','F','i','l','e',' ','s','i','z','e',' ','g','r','e','a','t','e','r',' ','t','h','a','n',' ','2','0','0','M','B','\r','\n'};
	byte[] tmp6 = {'H','T','T','P','/','1','.','1',' ','4','0','4',' ','C','h','e','c','k',' ','F','i','l','e',' ','n','a','m','e',' ','o','r',' ','D','i','r','e','c','t','o','r','y','\r','\n'};
	byte[] tmp7 = {'I','P',':',' ','E','R','R','O','R',' ','(','U','n','k','n','o','w','n','H','o','s','t',')','\n'};
	byte[] tmp8 = {'I','P',':',' ','E','R','R','O','R',' ','(','I','O',' ','E','r','r','o','r',')','\n'};
	byte[] tmp10= {'Q','u','i','t'};
	byte[] tmp11= {'\r','\n'};
	byte[] tmp12= {'C','a','n','t',' ','c','h','e','c','k',' ','f','o','r',' ','f','i','l','e',' ','e','x','i','s','t','e','n','c','e','\r','\n'};
	try
    { 
		Socket p= new Socket(PREFERRED, 21);   // peer, connection to HOST
		InputStream ftpin = p.getInputStream();
        OutputStream ftpout    = p.getOutputStream();
        OutputStream out=c.getOutputStream();
	    byte[] e=new byte[1024];
	 	byte[] d=null;
	 	ftpin.read(e);
	 	// string to read message from input 
    	d=new byte[1024];
    	ftpout.write(ftpuser);
		ftpin.read(d);
    	ftpout.write(ftppass);
    	d=new byte[1024];
    	ftpin.read(d);
    	ftpout.write(ftpsize);
    	d=new byte[1024];
    	int flen=ftpin.read(d);
    	if(AscbToInt(d, 0, 3)==550)
    	{response=tmp12;out.write(response);p.close();return -1;}
    	if(AscbToInt(d, 4, (flen-6))>209715200)
    	{response=tmp5;out.write(response);p.close();return -1;}
		ftpout.write(ftpapsv);
    	d=new byte[1024];
		byte[] ipport=new byte[1024];
		int totbytes=0;
    	totbytes=ftpin.read(ipport);
		int port=0;
		int ipstart=0,ipend=0,portst=0,portend=0,count=0,med=0;
		for(int i=0;i<totbytes;i++)
		{
					if(ipport[i]==40)
						ipstart=i+1;
					else if(ipport[i]==44 && count<3)
						{count++;ipport[i]=46;}
					else if(ipport[i]==44 && count==3)
						{ipend=i-1;portst=i+1;count++;}
					else if(ipport[i]==44 && count==4)
					{med=i;count++;}
					else if(ipport[i]==41)
						{portend=i-1;break;}
		}
		byte[] ip=new byte[ipend-ipstart+1];
		for(int i=ipend;i>=ipstart;i--)
		{
			ip[i-ipstart]=ipport[i];
		}
		port=AscbToInt(ipport, portst, (med-portst))*256+AscbToInt(ipport, med+1, (portend-med));
		Socket socket1 = new Socket(InetAddress.getByName(byteToString(ip, 0, ip.length)), port); 
        ftpout.write(ftpretr);		 // trim  
        InputStream dataout=null;
        dataout=socket1.getInputStream(); 
        d=new byte[1024];
        totbytes=ftpin.read(d);
        if(d[0]==49)
        {while(true)
        {d=new byte[65000];
        tmpbytes=dataout.read(d);
        totalbytes+=tmpbytes;
        if(tmpbytes>0)
        	out.write(d,0,tmpbytes);
        else 
        	{out.write(tmp11);socket1.close();break;}}}
        else
        	{out.write(tmp6);socket1.close();p.close();return -1;};
		ftpout.write(tmp10);
    	d=new byte[1024];
    	totbytes=ftpin.read(d);
        p.close();
    }
	catch (UnknownHostException e) {
		response= tmp7;
	} catch (IOException e) {
		// TODO Auto-generated catch block
		response= tmp8;
	}
return totalbytes;
}

int run(int X)   // NOC - do not change the signature for run()
{
  ServerSocket s0 = null; // NOC - this is the listening socket
  Socket       s1 = null; // NOC - this is the accept-ed socket i.e. client
  byte []      b0=null;  // ADC - general purpose buffer
  byte[] tmp9 = {'H','T','T','P','/','1','.','1',' ','4','0','4',' ','N','o','t',' ','F','o','u','n','d','\r','\n'};
  int loop=0;
  // ADC here
  try {
  s0 = new ServerSocket();
  //InetAddress inetAddress=InetAddress.getByName("localhost");
  s0.bind(new InetSocketAddress(PORT),200);
  System.out.println("Apache Listening on socket "+ PORT);

  while ( true ) {        // NOC - main loop, do not change this!
    // ADC from here to LOOPEND : add or change code
	  loop++;
	  totbyt=0;
	  b0=new byte[1024];
      byte[] tmp= {'R','E','Q',' ','\r','\n','I','P',':',' '};
	  byte[] bckhost= new byte[1024];
	  s1= s0.accept() ;
	  System.out.println("("+loop+") Incoming client connection from ["+s1.getInetAddress().getHostName()+":"+s1.getPort()+"] to me ["+s0.getInetAddress().getHostName()+":"+s0.getLocalPort()+"]");
	  totbyt=s1.getInputStream().read(b0);
	  if(totbyt>=0 && b0[0]>=0)
	  {   reqb0=mrg(b0,0,totbyt);
		  for(int i=0;i<totbyt;i++)
	  {
		  if(b0[i]==10)
			  totbyt=i+1;
		  else if(b0[i]==72 && b0[i+1]==111 && b0[i+2]==115 && b0[i+3]==116 && b0[i+4]==58)
		  {for (int j = i+6; j < totbyt; j++) 
		  {  	if(b0[j]==13)
			  		break;
		  		bckhost[j-(i+6)]=b0[j];}
		  }
	  }
		  if(parse(b0)>=0)
		 {int dnsres=dns(0);  // sets PREFERRED  
    // Part 1 - do this:
	  if(reqtype==0)
	  {System.out.println("    REQ "+byteToString(HOST, 0, HOST.length-1)+" / "+byteToString(response, 0, response.length-1));
      OutputStream out=s1.getOutputStream();
      out.write(mrg(mrg(mrg(tmp, 0, 4, HOST, 0, HOST.length),0,(HOST.length+4),tmp,4,6),response));}// Part 1 ends here
	  else if(reqtype==1)
	  {
	  if(dnsres>=0)
		  System.out.println("    REQ "+byteToString(url, 0, url.length-1)+" ("+http_fetch(s1)+" bytes transfered)");
	  else if (dnsres<0)
		  {host= new byte[1024];
		  extra= new byte[1024];
		  int hostlen=s1.getInputStream().read(host);
		  s1.getInputStream().read(extra);
		  if(url[0]==47)
		  {for(int i=6;i<hostlen-2;i++)
			  HOST[i-6]=host[i];}
		  if(dns(0)>=0)
			  System.out.println("    REQ "+byteToString(url, 0, url.length-1)+" ("+http_fetch(s1)+" bytes transfered)");
		  else
		  System.out.println("    REQ "+byteToString(url, 0, url.length-1)+" / "+byteToString(response, 0, response.length-1));}
	  }
	  else if(reqtype==2)
	  {
	  if(dnsres>=0)
	  {int ftplen=ftp_fetch(s1);
	  	if(ftplen>=0)
	  		System.out.println("    REQ "+byteToString(url, 0, url.length-1)+" ("+ftplen+" bytes transfered)");
	  	else
	  		System.out.println("    REQ "+byteToString(url, 0, url.length-1)+" / "+byteToString(response, 0, response.length-1));}
	  else
		  {
		    s1.getOutputStream().write(tmp9);
	  		System.out.println("    REQ "+byteToString(url, 0, url.length-1)+" / "+byteToString(response, 0, response.length-1));
		  };
		  
	  }
	  else if(reqtype<0)
	  {System.out.println("    "+byteToString(reqb0, 0, reqb0.length-1)+" / "+byteToString(response, 0, response.length-1));
      OutputStream out=s1.getOutputStream();
      out.write(mrg(reqb0, response));}}
	  else
	  {
		  System.out.println("    REQ "+byteToString(reqb0, 0, reqb0.length-1)+" / "+byteToString(response, 0, response.length-1));
      OutputStream out=s1.getOutputStream();
      out.write(mrg(mrg(mrg(tmp, 0, 4, reqb0, 0, reqb0.length),0,(reqb0.length+4),tmp,4,6),response));
	  }
	  }
      s1.close();
  } // NOC - main loop
  }
  catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
  return 0;
  }


String byteToString( byte [] b , int start, int len ) {
	
	char[] temp=new char[len];
	for (int i = 0; i< len; i++) {
        temp[i] = (char)(b[i + start] & 0xff);
    }
	return new String(temp);
}

int AscbToInt(byte[] b1,int off, int len) {
    // your code here must use this to convert "b" to int
	int n = 0;
	for (int i=0;i<len;i++)
		n = 10*n + (b1[off+i]-'0');
	return n;
}

boolean cmp(byte[] b, int start, int len, byte[] target)
{
	for (int i = start; i < (start+len); i++) {
		if(b[i]!=target[i-start])
				return false;
	}
return true;
}

byte[] mrg(byte[] a, int starta, int lena, byte[] b, int startb, int lenb)
	{byte[] tmp= new byte[lena+lenb];
		for (int i = starta; i < lena; i++) 
			tmp[i-starta]=a[i];
		for (int i = startb; i < lenb; i++) 
			{tmp[lena+(i-startb)]=b[i];}
	return tmp;
	}

byte[] mrg(byte[] a, byte[] b)
{
	int starta=0, startb=0, lena=a.length, lenb=b.length;
	byte[] tmp= new byte[lena+lenb];
	for (int i = starta; i < lena; i++) 
		tmp[i-starta]=a[i];
	for (int i = startb; i < lenb; i++) 
		tmp[lena+(i-startb)]=b[i];
return tmp;
}

byte[] mrg(byte[] a, int starta, int lena)
{byte[] tmp= new byte[lena];
	for (int i = starta; i < lena; i++) 
		tmp[i-starta]=a[i];
return tmp;
}

byte[] replace(byte[] a, int starta, int lena, byte[] b, byte[] c)
{	boolean present=false;
	int j=0,m=0, i=0;
	byte[] value=new byte[lena];
	for (i = 0; i < lena; i++) {
		m=i;
		for (j = 0; j < b.length; j++) {
			if(a[m]==b[j])
				m++;
			else 
				break;}
		if(j==b.length)
			{present=true;
			for (int k = i; k < m+2; k++)
				value[k]=a[k];
			break;}
		value[i]=a[i];}
	if(present)
	{for (int k = i; k < (i+c.length); k++) {
		m=k;
		value[k]=c[k-i];}
	i=m;
	for (int k = i; k < lena; k++)
	{	
		if(a[k]==13 && a[k+1]==10)
			{i=k;break;}
	}
	for (int k = i; k < lena; k++) {
		m++;value[m]=a[k];}
	}
	trim(value);
	return value;
}

byte[] trim(byte[] a)
{int i=0;
	for (i = 0; i < a.length; i++) 
		{if(a[i]==0)
			break;}
byte[] tmp= new byte[i];
	for (int j = 0; j < i; j++) 
		tmp[j]=a[j];
return tmp;
}
} // class Apache
