
var cordova = require('cordova');
module.exports = {
	/**
	 * 检查QQ客户端是否安装
	 * 
	 * @param {*} isPermissionGranted 
	 */
	checkClientInstalled: function(client) {
		return new Promise(function(resolve, reject) {
			if (client == undefined) {
				client = 0;
			}
			cordova.exec((res) => {
				resolve(res);
			  }, (res) => {
				reject(res);
			  }, "QQSDK", "checkClientInstalled", [{
					client: client
				}]);
		});
	},
	
	/**
	 * 在调用其他SDK接口前调用该接口通知 SDK 用户是否已授权应用获取设备信息的权限，或在应用的取消授权界面中提供用户撤销获取设备信息的权限
	 * 
	 * @param {*} isPermissionGranted 
	 */
  setIsPermissionGranted: function(isPermissionGranted) {
		return new Promise(function(resolve, reject) {
			cordova.exec((res) => {
				resolve(res);
			  }, (res) => {
				reject(res);
			  }, "QQSDK", "setIsPermissionGranted", [{
				isPermissionGranted: isPermissionGranted
			}]);
		});
	},

	/**
	 * 调起QQ登录
	 */
	login: function() {
		return new Promise(function(resolve, reject) {
			cordova.exec((res) => {
				resolve(res);
			  }, (res) => {
				reject(res);
			  }, "QQSDK", "login", []);
		});
	},
	
	/**
	 * 退出QQ登录
	 */
	logout: function() {
		return new Promise(function(resolve, reject) {
			cordova.exec((res) => {
				resolve(res);
			  }, (res) => {
				reject(res);
			  }, "QQSDK", "logout", []);
		});
	},

	/**
	 * 分享到QQ
	 * @param {object} params 
	 */
	shareToQQ: function(params) {
		return new Promise(function(resolve, reject) {
			if(params === undefined) {
				params = {}
			}
			cordova.exec((res) => {
				resolve(res);
			  }, (res) => {
				reject(res);
			  }, "QQSDK", "shareToQQ", [params]);
		});
	},

	/**
	 * 分享到QQ空间
	 * @param {object} params 
	 */
	shareToQzone: function(params) {
		return new Promise(function(resolve, reject) {
			if(params === undefined) {
				params = {}
			}
			cordova.exec((res) => {
				resolve(res);
			  }, (res) => {
				reject(res);
			  }, "QQSDK", "shareToQzone", [params]);
		});
	},
};
