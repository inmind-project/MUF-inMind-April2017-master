/*
 * Copyright (C) Carnegie Mellon University - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * This is proprietary and confidential.
 * Written by members of the ArticuLab, directed by Justine Cassell, 2018.
 */

package edu.cmu.inmind.multiuser.net;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import edu.cmu.inmind.multiuser.controller.log.Log4J;

/**
 * Finds the preferred {@link InetAddress} found for the system, first preferring network interfaces with the word "Ethernet" in their name and then preferring those with the greatest MTU.
 */
public final class PreferredInetAddressFinder {

	private static final Pattern LAN_ADAPTER_NAME_PATTERN = Pattern.compile("^e(?:(?:th)|(?:np))\\d+.*$", Pattern.CASE_INSENSITIVE);

	private static final List<String> WIFI_KEYWORDS = Arrays.asList("wi-fi", "wifi", "wireless");

	private static final Comparator<NetworkInterface> LAN_NAME_COMPARATOR = (o1, o2) -> {
		final boolean b1 = isLANAdapter(o1);
		final boolean b2 = isLANAdapter(o2);

		final int result;
		if (b1) {
			if (b2) {
				result = 0;
			} else {
				result = -1;
			}
		} else if (b2) {
			result = 1;
		} else {
			result = 0;
		}
		return result;
	};

	private static final Comparator<NetworkInterface> WIFI_NAME_COMPARATOR = (o1, o2) -> {
		final boolean b1 = isWifiAdapter(o1);
		final boolean b2 = isWifiAdapter(o2);

		final int result;
		if (b1) {
			if (b2) {
				result = 0;
			} else {
				result = -1;
			}
		} else if (b2) {
			result = 1;
		} else {
			result = 0;
		}
		return result;
	};

	private static final Comparator<NetworkInterface> INTERFACE_SORTER = LAN_NAME_COMPARATOR.thenComparing(WIFI_NAME_COMPARATOR.reversed()).thenComparingInt(PreferredInetAddressFinder::getInverseMTUUnchecked);

	private static int getInverseMTUUnchecked(final NetworkInterface netInt) {
		try {
			return -netInt.getMTU();
		} catch (final SocketException e) {
			throw new UncheckedIOException(e);
		}
	}

	private static Stream<NetworkInterface> getValidInterfaces(final Enumeration<NetworkInterface> netInts) throws SocketException {
		final Stream.Builder<NetworkInterface> resultBuilder = Stream.builder();
		while (netInts.hasMoreElements()) {
			final NetworkInterface netInt = netInts.nextElement();
			if (isValid(netInt)) {
				resultBuilder.add(netInt);
			}
		}
		return resultBuilder.build();
	}

	private static boolean hasWifiKeywords(final String str) {
		return WIFI_KEYWORDS.stream().anyMatch(str::contains);
	}

	private static boolean isLANAdapter(final NetworkInterface netInt) {
		final boolean result;

		final String normalizedDispName = normalizeDispName(netInt.getDisplayName());
		// First check for wi-fi keywords because some names look like e.g. "Wireless Ethernet adapter"
		if (hasWifiKeywords(normalizedDispName)) {
			result = false;
		} else {
			final String substr = "ethernet";
			if (normalizedDispName.contains(substr)) {
				result = true;
			} else {
				// Try looking at the adapter ID
				final String id = netInt.getName();
				final Matcher m = LAN_ADAPTER_NAME_PATTERN.matcher(id);
				result = m.matches();
			}
		}

		return result;
	}

	private static boolean isValid(final NetworkInterface netInt) throws SocketException {
		return !netInt.isLoopback() && !netInt.isVirtual();
	}

	private static boolean isWifiAdapter(final NetworkInterface netInt) {
		final String normalizedDispName = normalizeDispName(netInt.getDisplayName());
		return hasWifiKeywords(normalizedDispName);
	}

	private static String normalizeDispName(final String name) {
		return name.toLowerCase();
	}

	private final int socketTimeout;

	public PreferredInetAddressFinder(final int socketTimeout) {
		this.socketTimeout = socketTimeout;
	}

	public PreferredInetAddressFinder() {
		this(1000);
	}

	/**
	 * Finds the preferred {@link InetAddress} found for the system, first preferring network interfaces with the word "Ethernet" in their name and then preferring those with the greatest MTU.
	 *
	 * @return The preferred {@code InetAddress} if found, else an empty {@link Optional}.
	 */
	public Optional<InetAddress> get() throws SocketException {
		Optional<InetAddress> result = Optional.empty();
		// https://docs.oracle.com/javase/tutorial/networking/nifs/listing.html
		final Iterable<NetworkInterface> validNetInts = getValidInterfaces(NetworkInterface.getNetworkInterfaces()).sorted(INTERFACE_SORTER)::iterator;
		for (final NetworkInterface netInt : validNetInts) {
			final Stream<InetAddress> validAddrs = getValidAddresses(netInt.getInetAddresses());
			// Currently, any valid IP address is accepted
			final Optional<InetAddress> preferredAddr = validAddrs.findAny();
			if (preferredAddr.isPresent()) {
				result = preferredAddr;
				break;
			}
		}
		return result;
	}

	private Stream<InetAddress> getValidAddresses(final Enumeration<InetAddress> addrs) {
		final Stream.Builder<InetAddress> resultBuilder = Stream.builder();
		while (addrs.hasMoreElements()) {
			final InetAddress addr = addrs.nextElement();
			if (isValid(addr)) {
				resultBuilder.add(addr);
			}
		}
		return resultBuilder.build();
	}

	private boolean isValid(final InetAddress addr) {
		boolean result = false;
		if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
			try {
				result = addr.isReachable(socketTimeout);
			} catch (final IOException e) {
				Log4J.warn(this, String.format("A(n) %s occurred while checking if IP address \"%s\" is reachable: %s", e.getClass().getName(), addr.getHostAddress(), e.getLocalizedMessage()));
			}
		}
		return result;
	}
}
