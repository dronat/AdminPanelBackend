package com.woop.Squad4J.a2s.response;

import com.ibasco.agql.protocols.valve.source.query.info.SourceQueryInfoResponse;
import com.ibasco.agql.protocols.valve.source.query.rules.SourceQueryRulesResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Class describing a "combined" A2S response. A combined A2S response contains a {@link A2SInfoResponse} and
 * a {@link A2SRulesResponse}.
 *
 * @author Robert Engle
 * @see A2SInfoResponse
 * @see A2SRulesResponse
 */
@AllArgsConstructor
@Getter
public class A2SCombinedResponse {
    //private final A2SInfoResponse info;
    //private final A2SRulesResponse rules;
    private final SourceQueryInfoResponse info;
    private final SourceQueryRulesResponse rules;
}
