glowWorm
========

   由于温少开源的FastJSON非常优秀，但由于JSON本身协议是需要传输属性名等字符串的，这就使得它在java系统间作为通讯或磁盘存储的序列化方案，就显得不合适了。所以，结合FastJSON的设计 + protocol buffer做了个序列化小东西(glowworm)。 
<br>
1.它的调用非常简单： 
  TP tmpTP = new TP(); 
   … … 
  // 序列化 
  byte[] tmpBytes = PB.toPBBytes(tmpTP); 
  // 反序列化 
  TP tmpTP_2 = (TP)PB.parsePBBytes(tmpBytes, TP.class); 
<br>
2.目前支持的类型包括： 
Map, HashMap, LinkedHashMap, TreeMap, ConcurrentMap, ConcurrentHashMap, Collection, List, ArrayList, Object, String,char, Character, byte, Byte, short, Short, int, Integer, float, Float, double, Double, long, Long, boolean, Boolean,byte[], short[], int[], long[].class, float[], double[], boolean[], char[], Object[] 
   作为早期版本，目前还不支持循环引用、引用、自定义输出格式等功能，但作为一个基本的二进制序列化工具，还是有它适用的场合。