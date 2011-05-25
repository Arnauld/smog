package org.technbolts.smog.util

import java.lang.reflect.{AccessibleObject, Method, Modifier}

object Reflects {
  private var sawSecurityException = false

  def getMethods(klazz: Class[_]): Array[Method] = {
    var methods: Array[Method] = null;
    try {
      // getDeclaredMethods may be rejected by the security manager
      // but getMethods is more expensive
      if (!sawSecurityException)
        methods = klazz.getDeclaredMethods
    } catch {
      case e: SecurityException =>
        // If we get an exception once, give up on getDeclaredMethods
        sawSecurityException = true;
    }
    if (methods == null) {
      methods = klazz.getMethods
    }

    methods.filter {
      m =>
        if (sawSecurityException)
          m.getDeclaringClass == klazz
        else
          Modifier.isPublic(m.getModifiers)
    }
  }

  def findUniqueOrNone(klazz: Class[_], name: String): Option[Method] =
    findUniqueOrNone(getMethods(klazz), name)

  private def findUniqueOrNone(methods: Array[Method], name: String): Option[Method] = {
    val founds = methods.filter {
      _.getName == name
    }
    if (founds.size > 1)
      throw new IllegalArgumentException("Multiple methods matching <" + name + ">")
    founds.headOption
  }

  def tryToMakeAccessible(accessibleObject: AnyRef): Boolean = {
    if (accessibleObject != null && accessibleObject.isInstanceOf[AccessibleObject]) {
      val accessible = accessibleObject.asInstanceOf[AccessibleObject]
      if (!accessible.isAccessible) {
        try {
          accessible.setAccessible(true);
        } catch {
          case e => //
        }
      }
      accessible.isAccessible
    }
    else false
  }

  def searchAccessibleMethod(method: Method, argTypes: Array[Class[_]]): Option[Method] = {
    if (notPublicOrStatic_?(method))
      return None

    val name = method.getName
    var c = method.getDeclaringClass
    if (!Modifier.isPublic(c.getModifiers))
      return None

    // search through interfaces first
    val intfMethod = findMethodFromInterfaces(c, name, argTypes)
    if (intfMethod.isDefined)
      return intfMethod

    // search through class hierarchy
    while (true) {
      c = c.getSuperclass
      if (c == null)
        return None

      if (Modifier.isPublic(c.getModifiers)) {
        try {
          val m = c.getMethod(name, argTypes:_*)
          if (publicAndNotStatic_?(m)) {
            return Some(m);
          }
        } catch {
          case ignored => //
        }
      }
    }
    None
  }

  private def notPublicOrStatic_?(m:Method):Boolean = {
    val modifiers = m.getModifiers
    val isStatic = Modifier.isStatic(modifiers)
    val isPublic = Modifier.isPublic(modifiers)
    !isPublic || isStatic
  }

  private def publicAndNotStatic_?(m:Method):Boolean = {
    val modifiers = m.getModifiers
    val isStatic = Modifier.isStatic(modifiers)
    val isPublic = Modifier.isPublic(modifiers)
    isPublic && !isStatic
  }

  def findMethodFromInterfaces(klazz: Class[_], name: String, args: Array[Class[_]]): Option[Method] = {
    val intfs = klazz.getInterfaces
    var method:Option[Method] = None
    intfs.find {
      intf => method = ownedMethod(intf, name, args)
              method.isDefined
    }
    method
  }

  def ownedMethod(intf: Class[_], name: String, argTypes: Array[Class[_]]): Option[Method] = {
    if (Modifier.isPublic(intf.getModifiers)) {
      try {
        Some(intf.getMethod(name, argTypes:_*))
      } catch {
        case ignore => None
      }
    }
    else
      None
  }

}