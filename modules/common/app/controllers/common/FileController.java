package controllers.common;

import com.google.common.io.Files;
import inceptors.common.Secured;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import play.Configuration;
import play.Logger;
import play.data.DynamicForm;
import play.data.Form;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;
import play.mvc.Security;
import sun.misc.BASE64Decoder;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
//import controllers.common.Live2dParser;

public class FileController extends XDomainController {
	private static int MAX_FILENAME_LENGTH = 50;
	@Security.Authenticated(Secured.class)
	public Result uploadFile() {
		String url = null;

		try {
			String id = session("userId");
						
			DynamicForm form = Form.form().bindFromRequest();
			// get file from the body
			String folder = form.get("folder");
					
			if (folder != null && !folder.isEmpty()) {
				if (!existFile(folder)) {
					throw new CodeException(ErrDefinition.E_COMMON_FTP_FOLDER_NOT_FOUND);
				}
				else {
					folder = id + File.separator + folder;
				}
			}
			else {
				folder = id;
			}
			
			MultipartFormData body = request().body().asMultipartFormData();
			List<FilePart> fileParts = body.getFiles();
			
			String fileName = null;
			File file = null;
			
			if (!fileParts.isEmpty()) {
				file = (File)fileParts.get(0).getFile();
				fileName = fileParts.get(0).getFilename();
			}
			//handle live2d
			Integer type = Integer.parseInt(form.get("type"));
			
//			if (type == 1) {
//				String encryptedFilename = CodeGenerator.generateShortUUId();
//				File encryptedFile = new File("/tmp/files/"+id+File.separator+"tmp/"+ encryptedFilename + ".mat");				
//				Files.createParentDirs(encryptedFile);
//				encryptedFile.createNewFile();
//				Live2dParser.encryptCryptoJs(session("userId"), file, encryptedFile);			
//
////				Live2dParser.encrypt(session("userId"), file, encryptedFile);
//				file.delete();
//				file = encryptedFile;
//				fileName = encryptedFilename + ".mat";
//			}
			
			Map<String, String> data = form.data();
			String imageData = data.get("base64Img");

			if (imageData != null) {
				//check the picture type
				int begin = imageData.indexOf('/');
				int end = imageData.indexOf(';');
				String suffix = imageData.substring(begin+1, end);
				int dataBegin = imageData.indexOf(',');
				imageData = imageData.substring(dataBegin+1);
				fileName = CodeGenerator.generateShortUUId() + "." + suffix;
				BASE64Decoder decoder = new BASE64Decoder();  
				byte[] d = decoder.decodeBuffer(imageData);
				if (null == file) {
					file = new File("/tmp/files/"+id+File.separator+"tmp/"+fileName);
		            Files.createParentDirs(file);
				}
				FileOutputStream osf = new FileOutputStream(file);
				osf.write(d);
				osf.flush();
				osf.close();
			}

			if (null != file) {
				boolean bIsImage = imageData != null ? true : false;
				if (file.getName().length() > MAX_FILENAME_LENGTH) {
					throw new CodeException(ErrDefinition.E_COMMON_FTP_FILENAME_TOO_LONG);
				}
				
				url = uploadFile(folder, fileName,
						new FileInputStream(file), /*!bIsImage*/true); //always change the name

				if (url == null) {
					throw new CodeException(ErrDefinition.E_COMMON_FTP_UPLOAD_FAILED);
				}
				
				//if this is a picture, return thumbnail
				if (bIsImage) {
					//get the new filename
					fileName = url.substring(url.lastIndexOf("/") + 1);
					//change it to small size
					BufferedImage img = ImageResizer.zoomImage(file, true, 540);
					ImageResizer.writeHighQuality(img, file.getAbsolutePath(), true);
					url = uploadFile(folder+"/thumbnail", fileName, new FileInputStream(file), false);
					if (url == null) {
						throw new CodeException(ErrDefinition.E_COMMON_FTP_UPLOAD_FAILED);
					}					
				}
			} else {
				throw new CodeException(ErrDefinition.E_COMMON_FTP_INCORRECT_FILE);
			}
			
			//clear the file
			if (!file.delete()) {
				Logger.error("delete failed");
			}
		}
		catch (CodeException ce) {
			Logger.error(ce.getMessage());
			return failure(ce.getCode());
		}
		catch (Throwable e) {
			e.printStackTrace();
			Logger.error(e.getMessage());
			return failure(ErrDefinition.E_COMMON_FTP_UPLOAD_FAILED);
		}

		return success("url", url);
	}


	public static String uploadFile(String dir, String filename, InputStream input, boolean changeName)
			throws UnsupportedEncodingException {
		filename = new String(filename.getBytes(), "UTF-8");
		// check whether it has suffix
		int suffixIndex = filename.lastIndexOf('.');

		String newFilename = filename;
		if (changeName) {
			newFilename = CodeGenerator.generateShortUUId();
			
			if (suffixIndex != -1) {
				newFilename += filename.substring(suffixIndex, filename.length());
			}
		}

		FTPClient ftp = new FTPClient();

		boolean isSuccess = false;
		try {
			int reply;
			ftp.setControlEncoding("UTF-8");
			String url = Configuration.root().getString("ftp.url");
			int port = Configuration.root().getInt("ftp.port");
			ftp.connect(url, port);

			String username = Configuration.root().getString("ftp.username");
			String password = Configuration.root().getString("ftp.password");
			ftp.login(username, password);
			reply = ftp.getReplyCode();
			//Logger.info(Integer.toString(reply));
			if (!FTPReply.isPositiveCompletion(reply)) {
				ftp.disconnect();
				return null;
			}

			ftp.setFileTransferMode(FTPClient.BINARY_FILE_TYPE);
			reply = ftp.getReplyCode();
			//Logger.info(Integer.toString(reply));
			ftp.setFileType(FTP.BINARY_FILE_TYPE);
			reply = ftp.getReplyCode();
			//Logger.info(Integer.toString(reply));
			String[] folders = dir.split("/");
			ftp.enterLocalPassiveMode();
			reply = ftp.getReplyCode();

			for (String folder : folders) {
				ftp.makeDirectory(folder);
				reply = ftp.getReplyCode();
				ftp.changeWorkingDirectory(folder);	
				reply = ftp.getReplyCode();
			}
			isSuccess = ftp.storeFile(newFilename, input);
			reply = ftp.getReplyCode();
			//Logger.info(Integer.toString(reply));
			input.close();
			ftp.logout();
		} catch (Throwable e) {
			e.printStackTrace();
			Logger.error(e.getMessage());
			isSuccess = false;
		} finally {
			if (ftp.isConnected()) {
				try {
					ftp.disconnect();
				} catch (IOException ioe) {
				}
			}
		}

		if (!isSuccess) {
			return null;
		}
		return Configuration.root().getString("domain") + "/wechat/common/files/"
				+ dir + "/" + newFilename;
	}

	private boolean isMobile(String userAgent) {
		if (userAgent != null) {
			if (userAgent.toLowerCase().contains("ipad")
					|| userAgent.toLowerCase().contains("iphone")
					|| userAgent.toLowerCase().contains("ipod")
					|| userAgent.toLowerCase().contains("android")) {
				return true;
			}
		}

		return false;
	}
		
	public static boolean createFolder(String dir) {

		FTPClient ftp = new FTPClient();

		boolean isSuccess = false;
		try {
			int reply;
			ftp.setControlEncoding("UTF-8");
			String url = Configuration.root().getString("ftp.url");
			int port = Configuration.root().getInt("ftp.port");
			ftp.connect(url, port);

			String username = Configuration.root().getString("ftp.username");
			String password = Configuration.root().getString("ftp.password");
			ftp.login(username, password);
			reply = ftp.getReplyCode();
			//Logger.info(Integer.toString(reply));
			if (!FTPReply.isPositiveCompletion(reply)) {
				ftp.disconnect();
				return false;
			}

			ftp.setFileTransferMode(FTPClient.BINARY_FILE_TYPE);
			reply = ftp.getReplyCode();
			//Logger.info(Integer.toString(reply));
			ftp.setFileType(FTP.BINARY_FILE_TYPE);
			reply = ftp.getReplyCode();
			//Logger.info(Integer.toString(reply));
			String[] folders = dir.split("/");
			ftp.enterLocalPassiveMode();
			reply = ftp.getReplyCode();

			for (String folder : folders) {
				ftp.makeDirectory(folder);
				reply = ftp.getReplyCode();
				ftp.changeWorkingDirectory(folder);	
				reply = ftp.getReplyCode();
			}
			
			//ftp.makeDirectory(dir);
			//reply = ftp.getReplyCode();
			if (reply == 250) {
				isSuccess = true;
			}
			Logger.info(Integer.toString(reply));
			ftp.logout();
		} catch (Throwable e) {
			e.printStackTrace();
			Logger.error(e.getMessage());
			isSuccess = false;
		} finally {
			if (ftp.isConnected()) {
				try {
					ftp.disconnect();
				} catch (IOException ioe) {
				}
			}
		}

		return isSuccess;
	}
	
	
	public static boolean deleteFolder(String dir) {

		FTPClient ftp = new FTPClient();

		boolean isSuccess = false;
		try {
			int reply;
			ftp.setControlEncoding("UTF-8");
			String url = Configuration.root().getString("ftp.url");
			int port = Configuration.root().getInt("ftp.port");
			ftp.connect(url, port);

			String username = Configuration.root().getString("ftp.username");
			String password = Configuration.root().getString("ftp.password");
			ftp.login(username, password);
			reply = ftp.getReplyCode();
			//Logger.info(Integer.toString(reply));
			if (!FTPReply.isPositiveCompletion(reply)) {
				ftp.disconnect();
				return false;
			}

			ftp.setFileTransferMode(FTPClient.BINARY_FILE_TYPE);
			reply = ftp.getReplyCode();
			//Logger.info(Integer.toString(reply));
			ftp.setFileType(FTP.BINARY_FILE_TYPE);
			reply = ftp.getReplyCode();
			//Logger.info(Integer.toString(reply));
			ftp.removeDirectory(dir);
			reply = ftp.getReplyCode();
//			if (reply == 200) {
				isSuccess = true;
//			}
			//Logger.info(Integer.toString(reply));
			ftp.logout();
		} catch (Throwable e) {
			e.printStackTrace();
			Logger.error(e.getMessage());
			isSuccess = false;
		} finally {
			if (ftp.isConnected()) {
				try {
					ftp.disconnect();
				} catch (IOException ioe) {
				}
			}
		}

		return isSuccess;
	}
	
	public static boolean deleteFiles(List<String> fileList) {
		FTPClient ftp = new FTPClient();

		boolean isSuccess = false;
		try {
			int reply;
			ftp.setControlEncoding("UTF-8");
			String url = Configuration.root().getString("ftp.url");
			int port = Configuration.root().getInt("ftp.port");
			ftp.connect(url, port);

			String username = Configuration.root().getString("ftp.username");
			String password = Configuration.root().getString("ftp.password");
			ftp.login(username, password);
			reply = ftp.getReplyCode();
			//Logger.info(Integer.toString(reply));
			if (!FTPReply.isPositiveCompletion(reply)) {
				ftp.disconnect();
				return false;
			}

			ftp.setFileTransferMode(FTPClient.BINARY_FILE_TYPE);
			reply = ftp.getReplyCode();
			//Logger.info(Integer.toString(reply));
			ftp.setFileType(FTP.BINARY_FILE_TYPE);
			reply = ftp.getReplyCode();
			//Logger.info(Integer.toString(reply));
			isSuccess = true;
			for (String filename : fileList) {
				ftp.deleteFile(filename);
//				reply = ftp.getReplyCode();
//				if (reply != 200) {
//					isSuccess = false;
//					break;
//				}				
			}
			//Logger.info(Integer.toString(reply));
			ftp.logout();
		} catch (Throwable e) {
			e.printStackTrace();
			Logger.error(e.getMessage());
			isSuccess = false;
		} finally {
			if (ftp.isConnected()) {
				try {
					ftp.disconnect();
				} catch (IOException ioe) {
				}
			}
		}

		return isSuccess;		
	}
	

	/**
	 * download
	 * 
	 * @param filePath
	 * @return
	 */
	public Result resource(String filePath) {
		try {
			filePath = java.net.URLDecoder.decode(filePath, "utf-8");
			String local = "/tmp/files" + File.separator;
			
			String filename = filePath;
			if (filePath.lastIndexOf('/') != -1) {
				local += filePath.substring(0, filePath.lastIndexOf('/') + 1);
				filename = filePath.substring(filePath.lastIndexOf('/') + 1);
			}

			boolean isPicture = false;
			if (filename.toLowerCase().endsWith(".jpg")
					|| filename.toLowerCase().endsWith(".png")) {
				isPicture = true;
			}

//			boolean isMobile = isMobile(request().getHeader("user-agent"));
//			if (isMobile && isPicture) {
//				local += "mobile" + File.separator;
//			}

			File downloadFile = new File(local + filename);
			if (downloadFile.exists()) {
				if(filePath.indexOf("live2d/",0)==0){
					response().setHeader("Content-Type","application/octet-stream");
				}
				response().setHeader(
					"Content-Disposition",
					"attachment;filename="
							+ new String(filePath.substring(
									filePath.lastIndexOf('/') + 1,
									filePath.length()).getBytes("utf-8"),
									"ISO-8859-1"));
				response().setHeader(CACHE_CONTROL, "max-age=1800");
				return ok(downloadFile);
			}

			downloadFile = getFtpFile(
					Configuration.root().getString("ftp.url"),
					Configuration.root().getString("ftp.username"),
					Configuration.root().getString("ftp.password"),
					Integer.valueOf(Configuration.root().getString("ftp.port")),
					filePath, local);

			if (downloadFile == null || !downloadFile.exists()) {
				return failure(ErrDefinition.E_COMMON_FTP_FILE_NOT_FOUND);
			}

//			if (isPicture) {
//				// resize it
//				BufferedImage img = null;
//				//if (isMobile) {
//					img = ImageResizer.zoomImage(downloadFile, true, ImageResizer.MAX_MOBILE_SIZE);
//					ImageResizer.writeHighQuality(img, local + filename, true);
////				} else {
////					img = ImageResizer.zoomImage(downloadFile, false, ImageResizer.MAX_MOBILE_SIZE);
////					ImageResizer.writeHighQuality(img, local + filename, false);
////				}
//			}
			if(filePath.indexOf("live2d/",0)==0){
				response().setHeader("Content-Type","application/octet-stream");
			}
			response().setHeader(
				"Content-Disposition",
				"attachment;filename="
						+ new String(filePath.substring(
								filePath.lastIndexOf('/') + 1,
								filePath.length()).getBytes("utf-8"),
								"ISO-8859-1"));
			response().setHeader(CACHE_CONTROL, "max-age=1800");
			return ok(downloadFile);
		} catch (Throwable e) {
			e.printStackTrace();
			Logger.error(e.getMessage());
			return null;
		}
	}

	/**
	 * get File
	 * 
	 * @param ftpUrl
	 * @param username
	 * @param password
	 * @param port
	 * @param filePath
	 * @param local
	 * @return
	 */
	private static File getFtpFile(String ftpUrl, String username, String password,
			Integer port, String filePath, String local) {
		File localFile = null;
		FTPClient ftpClient = null;
		try {
			ftpClient = new FTPClient();
			// ftp connect
			ftpClient.setControlEncoding("UTF-8");
			ftpClient.connect(ftpUrl, port);
			int reply = ftpClient.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply)) {
				ftpClient.disconnect();
			}
			File dirFile = new File(local);
			if (!dirFile.exists()) {
				dirFile.mkdirs();
			}
			ftpClient.setControlEncoding("utf8");
			boolean loginFlag = ftpClient.login(username, password);
			if (loginFlag) {
				localFile = download(filePath, ftpClient, local);
			}

			ftpClient.logout();
		} catch (SocketException e) {
			return null;
		} catch (IOException e) {
			return null;
		} finally {
			if (ftpClient.isConnected()) {
				try {
					ftpClient.disconnect();
				} catch (IOException ioe) {
				}
			}
		}
		return localFile;
	}

	/**
	 * ftpDownload
	 * 
	 * @param ftpFileName
	 * @param ftpClient
	 * @return
	 */
	public static File download(String ftpFileName, FTPClient ftpClient, String local) {
		File localfile = null;
		try {
			OutputStream fos = null;
			File file = new File(ftpFileName);
			localfile = new File(local + file.getName());

			ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
			ftpClient.setFileTransferMode(FTPClient.BINARY_FILE_TYPE);

			if (localfile.exists()) {
				file.delete();
			}
			fos = new FileOutputStream(localfile);
			boolean bRet = ftpClient.retrieveFile(ftpFileName, fos);

			fos.flush();
			fos.close();

			if (!bRet) {
				localfile.delete();
			}

		} catch (Exception e) {
			e.printStackTrace();
			Logger.error(e.getMessage());
		}

		return localfile;
	}
	
	
	public static boolean existFile(String fullFilename) {
		boolean bRet = false;
		try {
			fullFilename = java.net.URLDecoder.decode(fullFilename, "utf-8");
			String ftpUrl = Configuration.root().getString("ftp.url");
			String username = Configuration.root().getString("ftp.username");
			String password = Configuration.root().getString("ftp.password");
			int port =Integer.valueOf(Configuration.root().getString("ftp.port"));
	
			FTPClient ftpClient = new FTPClient();
			// ftp connect
			ftpClient.setControlEncoding("UTF-8");
			ftpClient.connect(ftpUrl, port);
			int reply = ftpClient.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply)) {
				ftpClient.disconnect();
			}
			ftpClient.setControlEncoding("utf8");
			boolean loginFlag = ftpClient.login(username, password);
			if (loginFlag) {
				ftpClient.getStatus(fullFilename);
				int retCode = ftpClient.getReplyCode();
				Logger.info(Integer.toString(retCode));
				if (retCode == 211||retCode == 213) {//550
					bRet = true;
				}
				else {
					//file does not exist
					bRet = false;					
				}				
			}

			ftpClient.logout();
					
		}
		catch (Throwable e) {
			e.printStackTrace();
			bRet = false;
		}
		
		return bRet;
	}
	
	public static File getFile(String filePath) {
		String tmpFile = "/tmp/files" + File.separator;
		String filename = filePath;
		if (filePath.lastIndexOf('/') != -1) {
			tmpFile += filePath.substring(0, filePath.lastIndexOf('/') + 1);
			filename = filePath.substring(filePath.lastIndexOf('/') + 1);
		}
		
		File downloadFile = getFtpFile(
				Configuration.root().getString("ftp.url"),
				Configuration.root().getString("ftp.username"),
				Configuration.root().getString("ftp.password"),
				Integer.valueOf(Configuration.root().getString("ftp.port")),
				filePath, tmpFile);
		
		return downloadFile;
	}	
	
	public static List<String> getFolder(String folder) {
		FTPClient ftp = new FTPClient();

		List<String> dirList = null;
		try {
			int reply;
			ftp.setControlEncoding("UTF-8");
			String url = Configuration.root().getString("ftp.url");
			int port = Configuration.root().getInt("ftp.port");
			ftp.connect(url, port);

			String username = Configuration.root().getString("ftp.username");
			String password = Configuration.root().getString("ftp.password");
			ftp.login(username, password);
			reply = ftp.getReplyCode();
			//Logger.info(Integer.toString(reply));
			if (!FTPReply.isPositiveCompletion(reply)) {
				ftp.disconnect();
				return null;
			}

			ftp.setFileTransferMode(FTPClient.BINARY_FILE_TYPE);
			reply = ftp.getReplyCode();
			//Logger.info(Integer.toString(reply));
			ftp.setFileType(FTP.BINARY_FILE_TYPE);
			reply = ftp.getReplyCode();
			//Logger.info(Integer.toString(reply));
			FTPFile[] files = ftp.listDirectories(folder);
			if (null != files) {
				dirList = new ArrayList<String>();
				for (FTPFile file : files) {
					if (file.getName().compareTo("thumbnail") != 0) {
						dirList.add(file.getName());						
					}
				}
			}
			ftp.logout();
		} catch (Throwable e) {
			e.printStackTrace();
			Logger.error(e.getMessage());
			return null;
		} finally {
			if (ftp.isConnected()) {
				try {
					ftp.disconnect();
				} catch (IOException ioe) {
				}
			}
		}

		return dirList;		
	}
	
	public static boolean copyServerFile(List<String> filesToCopy, String newFolder, boolean bIsFolder, boolean bIsCloneDir) {
		FTPClient ftp = new FTPClient();
		try {
			ftp.setControlEncoding("UTF-8");
			String url = Configuration.root().getString("ftp.url");
			int port = Configuration.root().getInt("ftp.port");
			ftp.connect(url, port);

			String username = Configuration.root().getString("ftp.username");
			String password = Configuration.root().getString("ftp.password");
			ftp.login(username, password);
			int reply = ftp.getReplyCode();
			//Logger.info(Integer.toString(reply));
			if (!FTPReply.isPositiveCompletion(reply)) {
				throw new CodeException(ErrDefinition.E_COMMON_FTP_UPLOAD_FAILED);
			}
			String defFtpDir = ftp.printWorkingDirectory();
			Logger.info(defFtpDir);

			ftp.setFileTransferMode(FTPClient.BINARY_FILE_TYPE);
			ftp.setFileType(FTP.BINARY_FILE_TYPE);
			
			//create the newFolder
			String dirs[] = newFolder.split("/");
			for (String dir : dirs) {
				ftp.makeDirectory(dir);
				boolean bIsSuccess = ftp.changeWorkingDirectory(dir);	
				if (!bIsSuccess) {
					throw new CodeException(ErrDefinition.E_COMMON_FTP_COPY_FAILED);
				}				
			}
			
			ByteArrayOutputStream os = null;
			InputStream is = null;
			boolean bIsSuccess = false;
			if (!bIsFolder) {
				try {
					for (String file : filesToCopy) {
						os = new ByteArrayOutputStream();
					    bIsSuccess = ftp.changeWorkingDirectory(defFtpDir);
						if (!bIsSuccess) {
							throw new CodeException(ErrDefinition.E_COMMON_FTP_COPY_FAILED);
						}						
						ftp.retrieveFile(file, os);
						is = new ByteArrayInputStream(os.toByteArray());
						os.close();
						
						bIsSuccess = ftp.changeWorkingDirectory(newFolder);
						if (!bIsSuccess) {
							throw new CodeException(ErrDefinition.E_COMMON_FTP_COPY_FAILED);
						}						
						
						dirs = file.split("/");
						
						if (bIsCloneDir) {
							for (int i = 0; i < dirs.length - 1; i++) {
								ftp.makeDirectory(dirs[i]);
								bIsSuccess = ftp.changeWorkingDirectory(dirs[i]);							
								if (!bIsSuccess) {
									throw new CodeException(ErrDefinition.E_COMMON_FTP_COPY_FAILED);
								}						
							}							
						}
						
						ftp.storeFile(dirs[dirs.length - 1], is);
						is.close();
					}										
				}
				catch (Throwable e) {
					throw e;
				}
				finally {
					try {
						if (null != os) {
							os.close();
						}
						
						if (null != is) {
							is.close();
						}
					}
					catch (Throwable e) {
						throw e;
					}
				}
			}
			else {
				try {
					//search the folder
					for (String rootFolder : filesToCopy) {
						//change to default folder
						bIsSuccess = ftp.changeWorkingDirectory(defFtpDir);
						if (!bIsSuccess) {
							throw new CodeException(ErrDefinition.E_COMMON_FTP_COPY_FAILED);
						}
												
						//change to rootFolder
						bIsSuccess = ftp.changeWorkingDirectory(rootFolder);
						if (!bIsSuccess) {
							throw new CodeException(ErrDefinition.E_COMMON_FTP_COPY_FAILED);
						}
						
						String originalFolder = ftp.printWorkingDirectory();
						FTPFile[] files = ftp.listFiles();
						
						//create basefolder
						bIsSuccess = ftp.changeWorkingDirectory(defFtpDir);
						if (!bIsSuccess) {
							throw new CodeException(ErrDefinition.E_COMMON_FTP_COPY_FAILED);
						}
						
						bIsSuccess = ftp.changeWorkingDirectory(newFolder);
						
						dirs = rootFolder.split("/");
						for (int i = 0; i < dirs.length; i++) {
							ftp.makeDirectory(dirs[i]);
							bIsSuccess = ftp.changeWorkingDirectory(dirs[i]);							
							if (!bIsSuccess) {
								throw new CodeException(ErrDefinition.E_COMMON_FTP_COPY_FAILED);
							}						
						}
						
						String baseFolder = ftp.printWorkingDirectory();
						searchFolder(ftp, files, is, os, originalFolder, baseFolder);
					}
				}
				catch (Throwable e) {
					throw e;
				}
				finally {
					try {
						if (null != os) {
							os.close();
						}
						
						if (null != is) {
							is.close();
						}
					}
					catch (Throwable e) {
						throw e;
					}
				}
			}
			
			return true;
		}
		catch (Throwable e) {
			e.printStackTrace();
			return false;
		}
		finally {
			if (ftp.isConnected()) {
				try {
					//delete the new folder first
					ftp.removeDirectory(newFolder);
					ftp.disconnect();
				} catch (IOException ioe) {
				}
			}			
		}
	}	
	
	private static void searchFolder(FTPClient ftp, FTPFile[] folders, InputStream is, ByteArrayOutputStream os, String originalFolder, String baseFolder) throws Throwable {
		boolean bIsSuccess = false;
		
		for (FTPFile folder : folders) {			
			if (folder.isDirectory()) {
				if (folder.getName().compareTo(".") == 0) {
					continue;
				}
				
				if (folder.getName().compareTo("..") == 0) {
					continue;
				}
				
				bIsSuccess = ftp.changeWorkingDirectory(baseFolder);
				if (!bIsSuccess) {
					throw new CodeException(ErrDefinition.E_COMMON_FTP_COPY_FAILED);
				}
				
				ftp.makeDirectory(folder.getName());
				
				//create the directory
				searchFolder(ftp, ftp.listFiles(originalFolder + "/" + folder.getName()), 
						is, os, originalFolder + "/" + folder.getName(),
						baseFolder + "/" + folder.getName());
			}
			else {
				//copy the original one
				bIsSuccess = ftp.changeWorkingDirectory(originalFolder);
				if (!bIsSuccess) {
					throw new CodeException(ErrDefinition.E_COMMON_FTP_COPY_FAILED);
				}
				//Logger.info("folder: " + originalFolder + " file: " + folder.getName());
				//it is a file, copy it
				os = new ByteArrayOutputStream();
				ftp.retrieveFile(folder.getName(), os);
				is = new ByteArrayInputStream(os.toByteArray());
				os.close();
				
				//to the target folder
				bIsSuccess = ftp.changeWorkingDirectory(baseFolder);
				if (!bIsSuccess) {
					throw new CodeException(ErrDefinition.E_COMMON_FTP_COPY_FAILED);
				}						

				ftp.storeFile(folder.getName(), is);
				is.close();
				
			}
		}
	}
}
