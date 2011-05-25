package org.technbolts.smog.js

import org.specs.Specification
import java.lang.String
import org.mozilla.javascript._
import Misc._
import org.technbolts.smog.util.Reflects
import test.JPlayer

class JsScriptSpecs extends Specification {

  "JsScript" should {
    "support simple use case" in {
      // Creates and enters a Context.
      // A Context stores information about the execution environment of a script.
      val context = Context.enter
      try {
        // Initializes the standard objects (Object, Function, etc.).
        // This must be done before scripts can be executed.
        val scope: Scriptable = context.initStandardObjects

        val result = context.evaluateString(scope, "Math.cos(Math.PI)", "<a string describing the source, such as a filename>", 1, null)
        result must haveClass[java.lang.Double]
        result.asInstanceOf[java.lang.Double].doubleValue must beCloseTo(-1.0, 0.0001)
      }
      finally {
        Context.exit()
      }
    }

    "support complex js (md5)" in {
      withinContext {
        ctx =>
          val scope: Scriptable = ctx.initStandardObjects
          val result = ctx.evaluateString(scope, md5Js + "\nMD5('admin')", "<a string describing the source, such as a filename>", 1, null)
          result must haveClass[String]
          result mustEqual md5Hex("admin")
      }
    }

    "support top level 'importPackage' function and java class" in {
      withinContext {
        ctx =>
          val scope: Scriptable = new ImporterTopLevel(ctx)
          val result = ctx.evaluateString(scope,
            """importPackage(org.technbolts.smog.js.test);

              var player = new JPlayer("Jake");
              player.gender = "female"; // this is a Java method!
              player.setGender("male"); // this the same Java method.
              player.age = 18; // this is a Java field
              player.age += 3;

              var player = new JPlayer("Jane");
              player.gender = "male";
              player.gender = "female";
              player.age = 19;
              player.age += 2;

              var count = JPlayer.PLAYER_COUNT;
              JPlayer.PLAYER_COUNT += 2;

            """, "<a string describing the source, such as a filename>", 1, null)
          println("res>" + result)
      }
    }

    "support sandbox" in {
      withinContext {
        ctx =>
          ctx.setClassShutter(new ClassShutter {
            def visibleToScripts(className: String) = {
              println("visibleToScripts>" + className)
              !(className.startsWith("org.technbolts.smog.js.test.JPlayer"))
            }
          })
          ctx.setInstructionObserverThreshold(1000)

          val scope: Scriptable = new ImporterTopLevel(ctx)
          ctx.evaluateString(scope,
            """importPackage(org.technbolts.smog.js.test);

              var player = new JPlayer("Jake");
            """, "<a string describing the source, such as a filename>", 1, null) must throwAn[org.mozilla.javascript.EcmaError]
      }
    }

    "support custom global but instance based function" in {
      withinContext {
        ctx =>
          val scope: ScriptableObject = ctx.initStandardObjects
          val player = new JPlayer("Bob")
          scope.defineProperty("f", new InstanceFunctionObject(player, "setTheAge"), ScriptableObject.DONTENUM)
          val res = ctx.evaluateString(scope, "f(2+6)", "instanceFunc.js", 1, null)
          player.age must_== 8
      }
    }
  }

  def withinContext(f: (Context) => Any) = {
    val context = Context.enter
    try {
      f(context)
    }
    finally {
      Context.exit()
    }
  }
}
