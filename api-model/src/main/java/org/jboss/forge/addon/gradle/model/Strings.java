package org.jboss.forge.addon.gradle.model;

/**
 * @author Adam Wyłuda
 */
class Strings
{
   public static boolean isNullOrEmpty(String string) {
      return string == null || string.isEmpty();
   }

   public static boolean compare(final String me, final String you)
   {
      // If both null or intern equals
      if (me == you)
         return true;

      // if me null and you are not
      if (me == null && you != null)
         return false;

      // me will not be null, test for equality
      return me.equals(you);
   }
}
