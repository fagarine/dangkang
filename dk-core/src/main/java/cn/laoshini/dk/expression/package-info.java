/**
 * 该包下实现表达式相关逻辑
 *
 * <p>
 * 提供将表达式做为业务逻辑执行的功能，为其他需要使用表达式功能的模块提供支持
 * </p>
 * <p>
 * 由于当前有许多表达式语言，如Spel, Groovy等，所以系统不再单独去实现一套，而是封装直接使用，本系统默认使用Spel
 * </p>
 *
 * @author fagarine
 */
package cn.laoshini.dk.expression;