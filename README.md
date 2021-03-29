第一步：

allprojects {

		repositories {
		
			
			maven { url 'https://jitpack.io' }
			
		}
	}
  
  第二步：
  
  dependencies {
  
	        implementation 'com.github.zoomself:exoplayer:Tag'
		
	}
	

实现：

1、一边播放一边缓存

2、列表间，不同页面间无缝丝滑切换
