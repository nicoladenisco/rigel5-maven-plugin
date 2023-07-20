package org.rigel5.plugin;

import java.io.File;
import org.apache.maven.plugin.testing.MojoRule;
import org.apache.maven.plugin.testing.WithoutMojo;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.Test;

public class RigelAdjustSchemaMojoTest
{
  public boolean bypassTest = false;

  @Rule
  public MojoRule rule = new MojoRule()
  {
    @Override
    protected void before()
       throws Throwable
    {
    }

    @Override
    protected void after()
    {
    }
  };

  /**
   * @throws Exception if any
   */
  @Test
  public void testSomething()
     throws Exception
  {
    if(bypassTest)
      return;

    File pom = new File("target/test-classes/project-to-test/");
    assertNotNull(pom);
    assertTrue(pom.exists());

    RigelAdjustSchemaMojo myMojo = (RigelAdjustSchemaMojo) rule.lookupConfiguredMojo(pom, "adjschema");
    assertNotNull(myMojo);

    File schemaDir = (File) rule.getVariableValueFromObject(myMojo, "schemaDir");
    assertNotNull(schemaDir);
    assertTrue(schemaDir.exists());

    myMojo.execute();

    File outputDirXml = (File) rule.getVariableValueFromObject(myMojo, "outputDirXml");
    assertNotNull(outputDirXml);
    assertTrue(outputDirXml.exists());

    File outputDirSql = (File) rule.getVariableValueFromObject(myMojo, "outputDirSql");
    assertNotNull(outputDirSql);
    assertTrue(outputDirSql.exists());

    File mxml = new File(outputDirXml, "application-schema.xml-new");
    assertTrue(mxml.exists());
    File msql = new File(outputDirSql, "application-schema.txt");
    assertTrue(msql.exists());
  }

  /** Do not need the MojoRule. */
  @WithoutMojo
  @Test
  public void testSomethingWhichDoesNotNeedTheMojoAndProbablyShouldBeExtractedIntoANewClassOfItsOwn()
  {
    assertTrue(true);
  }
}
