# ZxingLib
# һ����ά�룬����ɨ�輰��ά��ͼƬ���ɿ�

Usage

- Step 1. Add the JitPack repository to your build file

allprojects {
    repositories {
        ...
        maven { url "https://jitpack.io" }
    }
}
- Step 2. Add the dependency

dependencies {
        compile 'com.github.Victor2018:ZxingLib:v1.0'
}

- Step 3.init in your Application

 @Override
    public void onCreate() {
        super.onCreate();
        //��ʼ��
        VolleyRequest.buildRequestQueue(this);
    }

- Step 4.extends BasePresenterImpl<T> impl your presenterImpl

@HttpParms(method = Request.Method.GET,url = "http://www.weather.com.cn/data/sk/101280601.html",responseCls = Weather.class)
@Override
public void sendRequest(T parm) {
	HttpInject.inject(this);
	super.sendRequest(parm);
}

# ��ע�����ߣ�
- ���䣺 victor423099@gmail.com
- ����΢��
- ![image](https://github.com/Victor2018/AppUpdateLib/raw/master/SrceenShot/sina_weibo.jpg)

## License

Copyright (c) 2017 Victor

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.