package com.zujihu.clothes.data;

import java.io.Serializable;

import android.graphics.Bitmap;

public class PictureModel implements Serializable {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;

	public int					_id;
	public int					type;
	public String				url;
	public String				mask_path;
	public Bitmap				bitmap;
	public String				desc;

	public PictureModel(int id, int type, String url, Bitmap bitmap, String desc, String path) {
		this._id = id;
		this.type = type;
		this.url = url;
		this.mask_path = path;
		this.bitmap = bitmap;
		this.desc = desc;
	}

	public PictureModel(int id, String url) {
		this._id = id;
		this.url = url;
	}

	public PictureModel() {

	}
}
