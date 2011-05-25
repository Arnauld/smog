package org.technbolts.smog.js

import org.technbolts.smog.util.Reflects
import org.mozilla.javascript._

object InstanceFunctionObject {
  def computeTagTypes(types: Array[Class[_]]) =
    types.map {
      k =>
        val typeTag = FunctionObject.getTypeTag(k)
        if (typeTag == FunctionObject.JAVA_UNSUPPORTED_TYPE)
          throw new IllegalArgumentException("Argument type <" + k + "> is not supported")
        typeTag.toInt
    }
}

class InstanceFunctionObject(val instance: AnyRef, val name: String) extends BaseFunction {

  // method is 'var' since it can be changed to make it accessible
  var method = Reflects.findUniqueOrNone(instance.getClass, name).getOrElse {
    throw new IllegalArgumentException("No method found named <" + name + ">")
  }
  val argTypes = method.getParameterTypes
  val argTypeTags: Array[Int] = InstanceFunctionObject.computeTagTypes(argTypes)

  val returnType = method.getReturnType
  val returnTypeVoid = (returnType == Void.TYPE)
  val returnTypeTag = if (returnTypeVoid) -1 else FunctionObject.getTypeTag(returnType)

  override def getFunctionName = name

  override def getLength = getArity

  override def getArity = argTypeTags.size

  override def call(cx: Context, scope: Scriptable, thisObj: Scriptable, args: Array[AnyRef]): AnyRef = {
    var index: Int = -1
    val invokeArgs = args.map({
      arg =>
        index = index + 1
        FunctionObject.convertArg(cx, scope, arg, argTypeTags(index))
    })

    val ret = invokeMethod(invokeArgs)
    if (returnTypeVoid) {
      Undefined.instance;
    } else if (returnTypeTag == FunctionObject.JAVA_UNSUPPORTED_TYPE) {
      cx.getWrapFactory.wrap(cx, scope, ret, null).asInstanceOf[AnyRef]
    } else {
      ret.asInstanceOf[AnyRef]
    }
  }

  def invokeMethod(args:Array[AnyRef]): AnyRef = {
    try {
      return method.invoke(instance, args:_*)
    } catch {
      case ex: IllegalAccessException =>
        val accessible = Reflects.searchAccessibleMethod(method, argTypes)
        if (accessible.isDefined) {
          method = accessible.get;
        } else {
          if (!Reflects.tryToMakeAccessible(method)) {
            throw Context.throwAsScriptRuntimeEx(ex)
          }
        }
        // Retry after recovery
        return method.invoke(instance, args:_*);
    }
  }
}