package com.btiao.service;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class P2PInfoMgr {
	static private P2PInfoMgr inst = null;
	static public P2PInfoMgr instance() {
		if (inst == null) {
			inst = new P2PInfoMgr();
		}
		return inst;
	}
	
	static public void main(String[] args) throws Exception {
		P2PInfoMgr mgr = P2PInfoMgr.instance();
		String city = "beijing";
		mgr.toDBGJ(city, 1, 1, 2, 2, 1000, 60, 2000);
		mgr.closeDB(city);
	}
	
	public boolean toDBGJ(String city, int p1x, int p1y, int p2x, int p2y, int dgj, int tgj, int dist) {
		Connection cn = null;
		try {
			cn = getCon(city, false);
			Statement s = cn.createStatement();
			insertGJ(s, p1x,p1y,p2x,p2y, dgj, tgj, dist);
			cn.commit();
			s.close();
			cn.close();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			if (cn != null) {
				try {
					cn.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return true;
	}
	public boolean toDBZJ(String city, int p1x, int p1y, int p2x, int p2y, int dzj, int tzj, int dist) {
		Connection cn = null;
		try {
			cn = getCon(city, false);
			Statement s = cn.createStatement();
			insertZJ(s, p1x,p1y,p2x,p2y, dzj, tzj, dist);
			cn.commit();
			s.close();
			cn.close();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			if (cn != null) {
				try {
					cn.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return true;
	}
	public boolean toDBDist(String city, int p1x, int p1y, int p2x, int p2y, int dist) {
		Connection cn = null;
		Statement s = null;
		try {
			cn = getCon(city, false);
			s = cn.createStatement();
			insertDist(s, p1x,p1y,p2x,p2y, dist);
			cn.commit();
			s.close();
			cn.close();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			if (s != null) {
				try {
					s.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (cn != null) {
				relCon(city, cn);
			}
		}
		return true;
	}
	public synchronized void closeDB(String city) throws Exception {
		try {
			Connection cn = getCon(city, false);
			cn.createStatement().execute("SHUTDOWN COMPACT");// IMMEDIATELY");
			cn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		synchronized (initCity) {
			initCity.put(city, false);
		}
	}
	public synchronized void init(String city) throws Exception {
		synchronized (initCity) {
			Boolean inited = initCity.get(city);
			if (inited != null && inited == true) {
				closeDB(city);
			}
			
			for (String fn : getDBFns(city)) {
				File file = new File(AllTgMgr.DBDIR+File.separator+fn);
				file.delete();
			}
			
			Connection cn = null;
			try {
				cn = getCon(city, true);
				Statement s = cn.createStatement();
				s.execute("SET FILES LOG FALSE"); //
				createTB(s);
				cn.commit();
				s.close();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				cn.close();
			}
			
			initCity.put(city, true);
		}
	}
	private List<String> getDBFns(String dbId) {
		List<String> r = new ArrayList<String>();
		r.add(dbId+".script");
		r.add(dbId+".properties");
		r.add(dbId+".log");
		r.add(dbId+".lck");
		r.add(dbId+".tmp");
		return r;
	}
	private void createTB(Statement s) throws Exception {
		String sql = "CREATE CACHED TABLE tb_p2pinfo_gj (" +
			"p1x INTEGER, p1y INTEGER, p2x INTEGER, p2y INTEGER," +
			"dgj INTEGER, tgj INTEGER, dist INTEGER," +
			"PRIMARY KEY(p1x,p1y,p2x,p2y)" +
			")";
		s.execute(sql);
		
		sql = "CREATE CACHED TABLE tb_p2pinfo_zj (" +
			"p1x INTEGER, p1y INTEGER, p2x INTEGER, p2y INTEGER," +
			"dzj INTEGER, tzj INTEGER, dist INTEGER," +
			"PRIMARY KEY(p1x,p1y,p2x,p2y)" +
			")";
		s.execute(sql);
		
		sql = "CREATE CACHED TABLE tb_p2pinfo_dist (" +
			"p1x INTEGER, p1y INTEGER, p2x INTEGER, p2y INTEGER," +
			"dist INTEGER," +
			"PRIMARY KEY(p1x,p1y,p2x,p2y)" +
			")";
		s.execute(sql);
	}
	private void insertGJ(Statement s, int p1x, int p1y, int p2x, int p2y, int dgj, int tgj, int dist) throws Exception {
		String sql = "INSERT INTO tb_p2pinfo_gj (p1x,p1y,p2x,p2y,dgj,tgj,dist) VALUES (" +
				p1x + "," +
				p1y + "," +
				p2x + "," +
				p2y + "," +
				dgj + "," +
				tgj + "," +
				dist +
				")";
		try {
			s.execute(sql);
		} catch (Exception e) {
			System.err.println("sql is:" + sql);
			throw e;
		}
	}
	private void insertZJ(Statement s, int p1x, int p1y, int p2x, int p2y, int dzj, int tzj, int dist) throws Exception {
		String sql = "INSERT INTO tb_p2pinfo_zj (p1x,p1y,p2x,p2y,dzj,tzj,dist) VALUES (" +
				p1x + "," +
				p1y + "," +
				p2x + "," +
				p2y + "," +
				dzj + "," +
				tzj + "," +
				dist +
				")";
		try {
			s.execute(sql);
		} catch (Exception e) {
			System.err.println("sql is:" + sql);
			throw e;
		}
	}
	private void insertDist(Statement s, int p1x, int p1y, int p2x, int p2y, int dist) throws Exception {
		String sql = "INSERT INTO tb_p2pinfo_dist (p1x,p1y,p2x,p2y,dist) VALUES (" +
				p1x + "," +
				p1y + "," +
				p2x + "," +
				p2y + "," +
				dist +
				")";
		try {
			s.execute(sql);
		} catch (Exception e) {
			System.err.println("sql is:" + sql);
			throw e;
		}
	}
	private synchronized Connection getCon(String city, boolean create) throws Exception {
		Connection cn = null;
		
		Map<Connection,String> cnBulk = cpool.get(city);
		if (cnBulk != null) {
			Set<Connection> cns = cnBulk.keySet();
			if (cns.size() !=0) {
				cn = cns.iterator().next();
				cnBulk.remove(cn);
				--cpool_size;
			}
		}
		cn = genCon(city, create);
		
		return cn;
	}
	private synchronized void relCon(String city, Connection cn) {
		if (cpool_size >= cpool_max_size) {
			try {
				cn.close();
			} catch (Exception e) {
				;
			}
			return;
		}
		++cpool_size;
		Map<Connection,String> cnBulk = cpool.get(city);
		if (cnBulk != null) {
			cnBulk.put(cn, null);
		} else {
			cnBulk = new HashMap<Connection,String>();
			cnBulk.put(cn, null);
			cpool.put(city, cnBulk);
		}
	}
	private Connection genCon(String city, boolean create) throws Exception {
		int times = 4;
		do {
			try {
				Connection cn = DriverManager.getConnection("jdbc:hsqldb:file:"+AllTgMgr.DBDIR+File.separator+city+(create?"":";ifexist=true"), "SA", "");
				return cn;
			} catch (Exception e) {
				try {
					Thread.sleep(100*5);
				} catch (Exception ee) {};
			}
		} while (--times > 0);
		
		return null;
	}
	private P2PInfoMgr() {}
	
	private Map<String,Map<Connection,String>> cpool =
			new HashMap<String,Map<Connection,String>>();
	private volatile int cpool_size = 0;
	private volatile int cpool_max_size = 100;
	private Map<String,Boolean> initCity = new HashMap<String,Boolean>();
//	private Map<String,Connection> city2Con = new HashMap<String,Connection>();
}
