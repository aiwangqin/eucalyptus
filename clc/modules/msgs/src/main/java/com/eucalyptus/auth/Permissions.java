/*************************************************************************
 * Copyright 2009-2015 Eucalyptus Systems, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 * Please contact Eucalyptus Systems, Inc., 6755 Hollister Ave., Goleta
 * CA 93117, USA or visit http://www.eucalyptus.com/licenses/ if you need
 * additional information or have any questions.
 *
 * This file may incorporate work covered under the following copyright
 * and permission notice:
 *
 *   Software License Agreement (BSD License)
 *
 *   Copyright (c) 2008, Regents of the University of California
 *   All rights reserved.
 *
 *   Redistribution and use of this software in source and binary forms,
 *   with or without modification, are permitted provided that the
 *   following conditions are met:
 *
 *     Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *     Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer
 *     in the documentation and/or other materials provided with the
 *     distribution.
 *
 *   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *   "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *   LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 *   FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 *   COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 *   INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 *   BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *   LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 *   CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 *   LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 *   ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *   POSSIBILITY OF SUCH DAMAGE. USERS OF THIS SOFTWARE ACKNOWLEDGE
 *   THE POSSIBLE PRESENCE OF OTHER OPEN SOURCE LICENSED MATERIAL,
 *   COPYRIGHTED MATERIAL OR PATENTED MATERIAL IN THIS SOFTWARE,
 *   AND IF ANY SUCH MATERIAL IS DISCOVERED THE PARTY DISCOVERING
 *   IT MAY INFORM DR. RICH WOLSKI AT THE UNIVERSITY OF CALIFORNIA,
 *   SANTA BARBARA WHO WILL THEN ASCERTAIN THE MOST APPROPRIATE REMEDY,
 *   WHICH IN THE REGENTS' DISCRETION MAY INCLUDE, WITHOUT LIMITATION,
 *   REPLACEMENT OF THE CODE SO IDENTIFIED, LICENSING OF THE CODE SO
 *   IDENTIFIED, OR WITHDRAWAL OF THE CODE CAPABILITY TO THE EXTENT
 *   NEEDED TO COMPLY WITH ANY SUCH LICENSES OR RIGHTS.
 ************************************************************************/

package com.eucalyptus.auth;

import static com.eucalyptus.auth.api.PolicyEngine.AuthorizationMatch;
import static com.eucalyptus.auth.principal.Principal.PrincipalType;
import static com.google.common.collect.Maps.newHashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.log4j.Logger;
import com.eucalyptus.auth.api.PolicyEngine;
import com.eucalyptus.auth.policy.PolicySpec;
import com.eucalyptus.auth.policy.key.Key;
import com.eucalyptus.auth.policy.key.Keys;
import com.eucalyptus.auth.principal.Account;
import com.eucalyptus.auth.principal.AccountFullName;
import com.eucalyptus.auth.principal.PolicyVersion;
import com.eucalyptus.auth.principal.User;
import com.eucalyptus.auth.principal.UserPrincipal;
import com.eucalyptus.context.Contexts;
import com.eucalyptus.context.IllegalContextAccessException;
import com.eucalyptus.util.Exceptions;
import com.google.common.collect.Maps;

public class Permissions {
  
	private static Logger LOG = Logger.getLogger( Permissions.class );
  
	private static PolicyEngine policyEngine;

	public static void setPolicyEngine( PolicyEngine engine ) {
		synchronized( Permissions.class ) {
			LOG.info( "Setting the policy engine to: " + engine.getClass( ) );
			policyEngine = engine;
		}
	}

	public static AuthContext createAuthContext(
			final UserPrincipal requestUser,
			final Map<String, String> evaluatedKeys
	) throws AuthException {
		return new AuthContext( requestUser, requestUser.getPrincipalPolicies( ), evaluatedKeys );
	}

	public static AuthContextSupplier createAuthContextSupplier(
			final UserPrincipal requestUser,
			final Map<String, String> evaluatedKeys
	) {
		return new AuthContextSupplier( ) {
			@Override
			public AuthContext get( ) throws AuthException {
				return createAuthContext( requestUser, evaluatedKeys );
			}
		};
	}

	public static AuthEvaluationContext createEvaluationContext(
			final String vendor,
			final String resourceType,
			final String action,
			final User requestUser,
			final Iterable<PolicyVersion> policies,
			final Map<String,String> evaluatedKeys
	) {
		return policyEngine.createEvaluationContext( PolicySpec.qualifiedName( vendor, resourceType ), PolicySpec.qualifiedName( vendor, action ), requestUser, evaluatedKeys, policies );
	}


	public static boolean isAuthorized(
			@Nonnull  final String vendor,
			@Nonnull  final String resourceType,
			@Nonnull  final String resourceName,
			@Nullable final AccountFullName resourceAccount,
			@Nonnull  final String action,
			@Nonnull  final AuthContextSupplier requestUser
	) {
		final String resourceAccountNumber = resourceAccount==null ? null : resourceAccount.getAccountNumber( );
		try {
			return isAuthorized( requestUser.get().evaluationContext( vendor, resourceType, action ), resourceAccountNumber, resourceName );
		} catch ( AuthException e ) {
			LOG.error( "Exception in resource access to " + resourceType + ":" + resourceName, e );
			return false;
		}
	}

	public static boolean isAuthorized(
		@Nonnull  final String vendor,
		@Nonnull  final String resourceType,
		@Nonnull  final String resourceName,
		@Nullable final Account resourceAccount,
		@Nonnull  final String action,
		@Nonnull  final AuthContext requestUser
	) {
		final String resourceAccountNumber = resourceAccount==null ? null : resourceAccount.getAccountNumber( );
		return isAuthorized( requestUser.evaluationContext( vendor, resourceType, action ), resourceAccountNumber, resourceName );
	}

	public static boolean isAuthorized(
		@Nonnull  final String vendor,
		@Nonnull  final String resourceType,
		@Nonnull  final String resourceName,
		@Nullable final AccountFullName resourceAccount,
		@Nonnull  final String action,
		@Nonnull  final AuthContext requestUser
	) {
		final String resourceAccountNumber = resourceAccount==null ? null : resourceAccount.getAccountNumber( );
		return isAuthorized( requestUser.evaluationContext( vendor, resourceType, action ), resourceAccountNumber, resourceName );
	}

	public static boolean isAuthorized(
			@Nonnull  final AuthEvaluationContext context,
			@Nullable final String resourceAccountNumber,
			@Nonnull  final String resourceName
	) {
		try {
			// If we are not in a request context, e.g. the UI, use a dummy contract map.
			// TODO(wenye): we should consider how to handle this if we allow the EC2 operations in the UI.
			final Map<Contract.Type, Contract> contracts = newHashMap();
			policyEngine.evaluateAuthorization( context, AuthorizationMatch.All, resourceAccountNumber, resourceName, contracts );
			pushToContext(contracts);
			return true;
		} catch ( AuthException e ) {
			LOG.debug( "Denied resource access to " + context.describe( resourceAccountNumber, resourceName ) + ": " + e.getMessage() );
		} catch ( Exception e ) {
			LOG.error( "Exception in resource access to " + context.describe( resourceAccountNumber, resourceName ), e );
		}
		return false;
	}

	public static boolean isAuthorized(
		final PrincipalType principalType,
		final String principalName,
		final PolicyVersion resourcePolicy,
		final String resourceType,
		final String resourceName,
		final AccountFullName resourceAccount,
		final String action,
		final User requestUser,
		final Iterable<PolicyVersion> policies,
		final Map<String,String> evaluatedKeys
	) {
		final String resourceAccountNumber = resourceAccount==null ? null : resourceAccount.getAccountNumber( );
		final AuthEvaluationContext context = policyEngine.createEvaluationContext( resourceType, action, requestUser, evaluatedKeys, policies, principalType, principalName );
		try {
			final Map<Contract.Type, Contract> contracts = newHashMap();
			policyEngine.evaluateAuthorization( context, resourcePolicy, resourceAccountNumber, resourceName, contracts );
			pushToContext( contracts );
			return true;
		} catch ( AuthException e ) {
			LOG.debug( "Denied resource access to " + context.describe( resourceAccountNumber, resourceName ) + ": " + e.getMessage() );
		} catch ( Exception e ) {
			LOG.error( "Exception in resource access to " + context.describe( resourceAccountNumber, resourceName ), e );
		}
		return false;
	}

	/**
	 * Test if perhaps authorized to perform the given action.
	 *
	 * <p>WARNING! This will not check conditions or evaluate authorization for a
	 * specific resource. This check is suitable for determining if a user does
	 * not have permission for an action but MUST NOT be used to authorize access
	 * to a specific resource.</p>
	 *
	 * @param vendor The vendor.
	 * @param action The action.
	 * @param requestUser The context for the requesting user.
	 * @return True if perhaps authorized.
	 */
	public static boolean perhapsAuthorized(
		@Nonnull  final String vendor,
		@Nonnull  final String action,
		@Nonnull  final AuthContextSupplier requestUser
	) {
		try {
			// If we are not in a request context, e.g. the UI, use a dummy contract map.
			final Map<Contract.Type, Contract> contracts = newHashMap();
			policyEngine.evaluateAuthorization( requestUser.get( ).evaluationContext( vendor, null, action ), AuthorizationMatch.Unconditional, null, "", contracts );
			return true;
		} catch ( AuthException e ) {
			LOG.debug( "Denied access for action " + action + ": " + e.getMessage() );
		} catch ( Exception e ) {
			LOG.error( "Exception in access for action " + action, e );
		}
		return false;
	}

	public static boolean canAllocate( String vendor, String resourceType, String resourceName, String action, AuthContext requestUser, Long quantity ) {
		return canAllocate( requestUser.evaluationContext( vendor, resourceType, action ), resourceName, quantity );
	}

	public static boolean canAllocate( String vendor, String resourceType, String resourceName, String action, AuthContextSupplier requestUser, Long quantity ) {
		try {
			return canAllocate( requestUser.get().evaluationContext( vendor, resourceType, action ), resourceName, quantity );
		} catch ( AuthException e ) {
			LOG.error( "Exception in resource allocation for " + resourceType + ":" + resourceName, e );
			return false;
		}
	}

	public static boolean canAllocate( AuthEvaluationContext context, String resourceName, Long quantity ) {
		try {
			policyEngine.evaluateQuota( context, resourceName, quantity );
			return true;
		} catch ( AuthException e ) {
			LOG.debug( "Denied resource allocation of " + context.describe( resourceName, quantity ), e );
		}
		return false;
	}

	public static Map<String,String> evaluateHostKeys( ) throws AuthException {
		try {
			return Maps.transformValues(
					Keys.getKeyInstances( Key.EvaluationConstraint.ReceivingHost ),
					Keys.value( )
			);
		} catch ( RuntimeException e ) {
			throw Exceptions.rethrow( e, AuthException.class );
		}
	}

	private static void pushToContext( final Map<Contract.Type, Contract> contracts ) {
		try {
			Contexts.lookup().setContracts( contracts );
		} catch ( IllegalContextAccessException e ) {
			LOG.debug( "Not in a request context", e );
		}
	}
}
