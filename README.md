# PaywhereSemanticRecognition
花哪儿语义识别核心模块2.0

本模块可以将自然语意下的一段文字（包括不限于中文数字等）解析为金额、日期以及相应的杂料，符合花哪儿记账所需要的识别功能。

您可以将以下测试文本输入进行测试：
```
2号买鞋25块

2018年6月7日吃饭200元

昨天滴滴花了八十八

聚会壹仟陆佰贰拾柒元肆角

大前天理发壹拾伍元捌角

饿了么付款一百五十块八毛

账户188****8888于06月06日12时26分在饿了么成功付款54.80元【支付宝】
```

### 调用方法
目前仅支持单句单语意识别，调用方法如下：
```
SemanticRecognition.getInstance().doRecognition(String 要输入的文段);
```

返回的数据存放在SemanticRecognition.RecognitionResponsePackage中，它是一个典型的JavaBean，可通过get、set方法进行存取：
```
getDate();      //获取日期
getMoney();     //获取金额
getOtherText(); //获取其他文段
```

### 开源协议
```
    Copyright (C) 2018 PaywhereSemanticRecognition

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
```
