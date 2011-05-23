/**
 * Copyright (C) 2009 hprange <hprange@gmail.com>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.wounit.rules;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.net.URL;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.model.Statement;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.wounit.exceptions.WOUnitException;
import com.wounit.model.FooEntity;
import com.wounit.stubs.StubTestCaseClass;
import com.wounit.stubs.WrongTypeForUnderTestStubTestCaseClass;

@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractEditingContextTest {
    protected static final String TEST_MODEL_NAME = "Test";

    @Mock
    protected Statement mockStatement;

    @Mock
    protected Object mockTarget;

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Test
    public void cannotCreateObjectUnderTestForNonEnterpriseObjectFields() throws Exception {
	AbstractEditingContextRule editingContext = createEditingContext(TEST_MODEL_NAME);

	WrongTypeForUnderTestStubTestCaseClass mockTarget = new WrongTypeForUnderTestStubTestCaseClass();

	thrown.expect(WOUnitException.class);
	thrown.expectMessage(is("Cannot create object of type java.lang.String.\n Only fields of type com.webobjects.eocontrol.EOEnterpriseObject can be annotated with @UnderTest."));

	editingContext.before(mockTarget);
    }

    @Test
    public void clearEditingContextChangesAfterTestExecution() throws Exception {
	AbstractEditingContextRule editingContext = createEditingContext(TEST_MODEL_NAME);

	editingContext.before(mockTarget);

	FooEntity foo = FooEntity.createFooEntity(editingContext);

	foo.setBar("test");

	editingContext.saveChanges();

	editingContext.after(mockTarget);

	editingContext = createEditingContext(TEST_MODEL_NAME);

	NSArray<FooEntity> result = FooEntity.fetchAllFooEntities(editingContext);

	assertThat(result.isEmpty(), is(true));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void createAndInsertObjectForFieldAnnotatedWithUnderTest() throws Exception {
	AbstractEditingContextRule editingContext = createEditingContext(TEST_MODEL_NAME);

	StubTestCaseClass mockTestCase = new StubTestCaseClass();

	editingContext.before(mockTestCase);

	EOEnterpriseObject objectUnderTest = mockTestCase.objectUnderTest();

	assertThat(objectUnderTest, notNullValue());

	NSArray<EOEnterpriseObject> insertedObjects = editingContext.insertedObjects();

	assertThat(insertedObjects, hasItem(objectUnderTest));
    }

    protected abstract AbstractEditingContextRule createEditingContext(String... modelNames);

    @Test
    public void disposeEditingContextAfterTestExecution() throws Throwable {
	AbstractEditingContextRule editingContext = Mockito.spy(createEditingContext(TEST_MODEL_NAME));

	editingContext.before(mockTarget);

	Mockito.verify(editingContext, Mockito.times(0)).dispose();

	editingContext.after(mockTarget);

	Mockito.verify(editingContext, Mockito.times(1)).dispose();
    }

    @Test
    public void doNotRemoveModelsNotLoadedByTheEditingContextRule() throws Throwable {
	URL url = getClass().getResource("/" + TEST_MODEL_NAME + ".eomodeld");

	EOModelGroup.defaultGroup().addModelWithPathURL(url);

	AbstractEditingContextRule editingContext = createEditingContext();

	editingContext.before(mockTarget);
	editingContext.after(mockTarget);

	assertThat(EOModelGroup.defaultGroup().modelNamed(TEST_MODEL_NAME), notNullValue());
    }

    @Test
    public void ensureEditingContextCleanUpIsTriggeredAfterTestExecution() throws Throwable {
	AbstractEditingContextRule editingContext = spy(createEditingContext(TEST_MODEL_NAME));

	InOrder inOrder = inOrder(editingContext, mockStatement);

	editingContext.apply(mockStatement, null, mockTarget).evaluate();

	inOrder.verify(mockStatement).evaluate();
	inOrder.verify(editingContext).after(mockTarget);
    }

    @Test
    public void ensureEditingContextCleanUpIsTriggeredEvenIfTestExecutionThrowsException() throws Throwable {
	AbstractEditingContextRule editingContext = spy(createEditingContext(TEST_MODEL_NAME));

	doThrow(new Throwable("test error")).when(mockStatement).evaluate();

	InOrder inOrder = inOrder(editingContext, mockStatement);

	try {
	    editingContext.apply(mockStatement, null, mockTarget).evaluate();
	} catch (Throwable exception) {
	    // DO NOTHING
	} finally {
	    inOrder.verify(mockStatement).evaluate();
	    inOrder.verify(editingContext).after(mockTarget);
	}
    }

    @Test
    public void ensureEditingContextInitializationIsTriggeredBeforeTestExecution() throws Throwable {
	AbstractEditingContextRule editingContext = spy(createEditingContext(TEST_MODEL_NAME));

	InOrder inOrder = inOrder(editingContext, mockStatement);

	editingContext.apply(mockStatement, null, mockTarget).evaluate();

	inOrder.verify(editingContext).before(mockTarget);
	inOrder.verify(mockStatement).evaluate();
    }

    @Test
    public void exceptionIfCannotFindModel() throws Exception {
	thrown.expect(IllegalArgumentException.class);
	thrown.expectMessage(is("Cannot load model named 'UnknownModel'"));

	createEditingContext("UnknownModel");
    }

    @Test
    public void loadMoreThanOneModel() throws Exception {
	createEditingContext(TEST_MODEL_NAME, "AnotherTest");

	EOModel result = EOModelGroup.defaultGroup().modelNamed(TEST_MODEL_NAME);

	assertThat(result, notNullValue());

	result = EOModelGroup.defaultGroup().modelNamed("AnotherTest");

	assertThat(result, notNullValue());
    }

    @Test
    public void loadOneModel() throws Exception {
	createEditingContext(TEST_MODEL_NAME);

	EOModel result = EOModelGroup.defaultGroup().modelNamed(TEST_MODEL_NAME);

	assertThat(result, notNullValue());
    }

    @Test
    public void loadOneModelInsideResourcesFolder() throws Exception {
	createEditingContext("AnotherTest");

	EOModel result = EOModelGroup.defaultGroup().modelNamed("AnotherTest");

	assertThat(result, notNullValue());
    }

    @Test
    public void lockEditingContextBeforeRunningTheTestCase() throws Exception {
	AbstractEditingContextRule editingContext = spy(createEditingContext());

	verify(editingContext, times(0)).lock();

	editingContext.before(mockTarget);

	verify(editingContext, times(1)).lock();
    }

    @Test
    public void removeModelsLoadedByTheTemporaryEditingContextAfterTestExecution() throws Throwable {
	AbstractEditingContextRule editingContext = createEditingContext(TEST_MODEL_NAME);

	editingContext.before(mockTarget);
	editingContext.after(mockTarget);

	assertThat(EOModelGroup.defaultGroup().modelNamed(TEST_MODEL_NAME), nullValue());
    }

    @Test
    @Ignore(value = "Revert may not be necessary")
    public void revertEditingContextChangesAfterRunningTheTestCases() throws Exception {
	AbstractEditingContextRule editingContext = spy(createEditingContext());

	editingContext.before(mockTarget);

	verify(editingContext, times(0)).revert();

	editingContext.after(mockTarget);

	verify(editingContext, times(1)).revert();
    }

    @After
    public void tearDown() {
	EOModelGroup modelGroup = EOModelGroup.defaultGroup();

	EOModel model = modelGroup.modelNamed(TEST_MODEL_NAME);

	if (model != null) {
	    modelGroup.removeModel(model);
	}
    }

    @Test
    public void unlockEditingContextAfterRunningTheTestCase() throws Exception {
	TemporaryEditingContext editingContext = spy(new TemporaryEditingContext());

	editingContext.before(mockTarget);

	verify(editingContext, times(0)).unlock();

	editingContext.after(mockTarget);

	// The internal disposal logic calls the unlock too
	verify(editingContext, times(2)).unlock();
    }
}
