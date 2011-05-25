package org.technbolts.smog.js.test

import reflect.BeanProperty

object Player {
  var PLAYER_COUNT = 5
}

class Player(val name:String) {
  @BeanProperty var gender:String = null
  @BeanProperty var age:Int = 0
}