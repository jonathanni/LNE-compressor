GNU FDL is for the sample image.
GNU GPL is for the library.

  Example compression of a sample image. Displays with other compression types.
  
  HOW TO USE LiNear Encoding:
  
  ---------- Deflation (Compression)
  
  Create a new CImage object:
  
  CImage a = new CImage("STRING_PATH");
  
  Create a new CCompressor:
  
  CCompressor b = new CCompressor(a);
  
  Choose to compress in LiNear Encoding:
  
  b.compressLNE("FILE_NAME");
  
  ---------- Inflation (Decompression)
  
  Create a new File:
  
  File a = new File("LNE_FILE_NAME");
  
  Create a new CInflater:
  
  CInflater b = new CInflater(a);
  
  Create a new BufferedImage:
  
  BufferedImage c = new BufferedImage(...);
  
  Inflate using LiNear Encoding:
  
  b.inflateLNE(null);
  
  Get the image:
  
  c = b.getImage().getRawImage();
  
  ---------- Nota Bena:
  
  You can chain commands, for example:
  
  b = new CInflater(a).inflateLNE(c).inflateLNE(d).inflateLNE(e)...
  
  but I wouldn't know why you wanted to inflate to multiple images when you can
  just copy!