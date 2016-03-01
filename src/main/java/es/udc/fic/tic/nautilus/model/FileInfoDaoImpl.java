package es.udc.fic.tic.nautilus.model;

import java.util.Calendar;
import java.util.List;

import javax.management.InstanceNotFoundException;

import org.springframework.stereotype.Repository;

@Repository("fileDao")
public class FileInfoDaoImpl extends GenericDaoImpl<FileInfo, Long> implements FileInfoDao {


	public int numberOfFiles() {
		return getSession().createQuery("SELECT f FROM FileInfo f").list().size();
	}

	@SuppressWarnings("unchecked")
	public List<FileInfo> findByName(String name) throws InstanceNotFoundException {
		return getSession().createQuery("SELECT f FROM FileInfo f WHERE "
				+ "f.name = :name")
				.setParameter("name", name)
				.list();
	}

	public FileInfo findByHash(String hash) throws InstanceNotFoundException {
		FileInfo file = (FileInfo) getSession().createQuery("SELECT f FROM FileInfo f WHERE "
				+ "f.hash = :hash")
				.setParameter("hash", hash).list().get(0);
		
		return file;
	}
	
	@SuppressWarnings("unchecked")
	public Long getAllSizes() {
		List<Long> sizes = getSession().createQuery("SELECT f.size FROM FileInfo f").list();
		long finalLong = 0;
		for (Long size : sizes) {
			finalLong += size;
		}
		return finalLong;
	}
	
	@SuppressWarnings("unchecked")
	public List<FileInfo> findAllExpiratedFiles() {
		// Get dateNow
		Calendar dateNow = Calendar.getInstance();
		
		return getSession().createQuery("SELECT f FROM FileInfo f WHERE f.dateLimit < :dateNow")
			.setParameter("dateNow", dateNow).list();
	}
}
