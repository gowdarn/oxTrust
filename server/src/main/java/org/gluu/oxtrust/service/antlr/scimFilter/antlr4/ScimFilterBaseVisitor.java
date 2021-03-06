/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */
// Generated by ANTLR 4.5.2
package org.gluu.oxtrust.service.antlr.scimFilter.antlr4;
import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;

/**
 * This class provides an empty implementation of {@link ScimFilterVisitor},
 * which can be extended to create a visitor which only needs to handle a subset
 * of the available methods.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public class ScimFilterBaseVisitor<T> extends AbstractParseTreeVisitor<T> implements ScimFilterVisitor<T> {
	/**
	 * {@InheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override public T visitScimFilter(ScimFilterParser.ScimFilterContext ctx) { return visitChildren(ctx); }
	/**
	 * {@InheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override public T visitATTR_PR(ScimFilterParser.ATTR_PRContext ctx) { return visitChildren(ctx); }
	/**
	 * {@InheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override public T visitLBRAC_EXPR_RBRAC(ScimFilterParser.LBRAC_EXPR_RBRACContext ctx) { return visitChildren(ctx); }
	/**
	 * {@InheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override public T visitATTR_OPER_EXPR(ScimFilterParser.ATTR_OPER_EXPRContext ctx) { return visitChildren(ctx); }
	/**
	 * {@InheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override public T visitEXPR_OR_EXPR(ScimFilterParser.EXPR_OR_EXPRContext ctx) { return visitChildren(ctx); }
	/**
	 * {@InheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override public T visitEXPR_OPER_EXPR(ScimFilterParser.EXPR_OPER_EXPRContext ctx) { return visitChildren(ctx); }
	/**
	 * {@InheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override public T visitNOT_EXPR(ScimFilterParser.NOT_EXPRContext ctx) { return visitChildren(ctx); }
	/**
	 * {@InheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override public T visitEXPR_AND_EXPR(ScimFilterParser.EXPR_AND_EXPRContext ctx) { return visitChildren(ctx); }
	/**
	 * {@InheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override public T visitATTR_OPER_CRITERIA(ScimFilterParser.ATTR_OPER_CRITERIAContext ctx) { return visitChildren(ctx); }
	/**
	 * {@InheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override public T visitLPAREN_EXPR_RPAREN(ScimFilterParser.LPAREN_EXPR_RPARENContext ctx) { return visitChildren(ctx); }
	/**
	 * {@InheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override public T visitCriteria(ScimFilterParser.CriteriaContext ctx) { return visitChildren(ctx); }
	/**
	 * {@InheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override public T visitOperator(ScimFilterParser.OperatorContext ctx) { return visitChildren(ctx); }
}