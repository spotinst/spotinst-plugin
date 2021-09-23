package hudson.plugins.spotinst.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ohadmuchnik on 26/05/2016.
 */
public enum AwsInstanceTypeEnum implements ISpotInstanceTypeEnum {

    T1Micro("t1.micro", 1),

    M1Small("m1.small", 1),

    M1Medium("m1.medium", 2),

    M1Large("m1.large", 4),

    M1Xlarge("m1.xlarge", 8),

    M3Medium("m3.medium", 1),

    M3Large("m3.large", 2),

    M3Xlarge("m3.xlarge", 4),

    M32xlarge("m3.2xlarge", 8),

    M4Large("m4.large", 2),

    M4Xlarge("m4.xlarge", 4),

    M42xlarge("m4.2xlarge", 8),

    M44xlarge("m4.4xlarge", 16),

    M410xlarge("m4.10xlarge", 40),

    T2Nano("t2.nano", 1),

    T2Micro("t2.micro", 1),

    T2Small("t2.small", 1),

    T2Medium("t2.medium", 2),

    T2Large("t2.large", 2),

    M2Xlarge("m2.xlarge", 6),

    M22xlarge("m2.2xlarge", 13),

    M24xlarge("m2.4xlarge", 26),

    Cr18xlarge("cr1.8xlarge", 88),

    I2Xlarge("i2.xlarge", 4),

    I22xlarge("i2.2xlarge", 8),

    I24xlarge("i2.4xlarge", 16),

    I28xlarge("i2.8xlarge", 32),

    Hi14xlarge("hi1.4xlarge", 35),

    Hs18xlarge("hs1.8xlarge", 35),

    C1Medium("c1.medium", 5),

    C1Xlarge("c1.xlarge", 20),

    C3Large("c3.large", 2),

    C3Xlarge("c3.xlarge", 4),

    C32xlarge("c3.2xlarge", 8),

    C34xlarge("c3.4xlarge", 16),

    C38xlarge("c3.8xlarge", 32),

    C4Large("c4.large", 2),

    C4Xlarge("c4.xlarge", 4),

    C42xlarge("c4.2xlarge", 8),

    C44xlarge("c4.4xlarge", 16),

    C48xlarge("c4.8xlarge", 32),

    Cc14xlarge("cc1.4xlarge", 1),

    Cc28xlarge("cc2.8xlarge", 1),

    G22xlarge("g2.2xlarge", 8),

    Cg14xlarge("cg1.4xlarge", 33),

    R3Large("r3.large", 2),

    R3Xlarge("r3.xlarge", 4),

    R32xlarge("r3.2xlarge", 8),

    R34xlarge("r3.4xlarge", 16),

    R38xlarge("r3.8xlarge", 32),

    D2Xlarge("d2.xlarge", 4),

    D22xlarge("d2.2xlarge", 8),

    D24xlarge("d2.4xlarge", 16),

    D28xlarge("d2.8xlarge", 32),

    G28xlarge("g2.8xlarge", 32),

    M416xlarge("m4.16xlarge", 64),

    P216xlarge("p2.16xlarge", 64),

    P28xlarge("p2.8xlarge", 32),

    P2xlarge("p2.xlarge", 4),

    R416xlarge("r4.16xlarge", 64),

    R42xlarge("r4.2xlarge", 8),

    R44xlarge("r4.4xlarge", 16),

    R48xlarge("r4.8xlarge", 32),

    R4large("r4.large", 2),

    R4xlarge("r4.xlarge", 4),

    T22xlarge("t2.2xlarge", 8),

    T2xlarge("t2.xlarge", 4),

    X116xlarge("x1.16xlarge", 64),

    X132xlarge("x1.32xlarge", 128),

    G34xlarge("g3.4xlarge", 16),
    G38xlarge("g3.8xlarge", 32),
    G316xlarge("g3.16xlarge", 64),
    F12xlarge("f1.2xlarge", 8),
    F116xlarge("f1.16xlarge", 64),
    I3large("i3.large", 2),
    I3xlarge("i3.xlarge", 4),
    I32xlarge("i3.2xlarge", 8),
    I34xlarge("i3.4xlarge", 16),
    I38xlarge("i3.8xlarge", 32),
    I316xlarge("i3.16xlarge", 64),
    P32xlarge("p3.2xlarge", 8),
    P38xlarge("p3.8xlarge", 32),
    P316xlarge("p3.16xlarge", 64),
    C5large("c5.large", 2),
    C5xlarge("c5.xlarge", 4),
    C52xlarge("c5.2xlarge", 8),
    C54xlarge("c5.4xlarge", 16),
    C59xlarge("c5.9xlarge", 36),
    C518xlarge("c5.18xlarge", 72),
    X1Exlarge("x1e.xlarge", 4),
    X1E2xlarge("x1e.2xlarge", 8),
    X1E4xlarge("x1e.4xlarge", 16),
    X1E8xlarge("x1e.8xlarge", 32),
    X1E16xlarge("x1e.16xlarge", 64),
    X1E32xlarge("x1e.32xlarge", 128),
    M5large("m5.large", 2),
    M5xlarge("m5.xlarge", 4),
    M52xlarge("m5.2xlarge", 8),
    M54xlarge("m5.4xlarge", 16),
    M58xlarge("m5.8xlarge", 32),
    M512xlarge("m5.12xlarge", 48),
    M524xlarge("m5.24xlarge", 96),
    H12xlarge("h1.2xlarge", 8),
    H14xlarge("h1.4xlarge", 16),
    H18xlarge("h1.8xlarge", 32),
    H116xlarge("h1.16xlarge", 64),

    C5D18xlarge("c5d.18xlarge", 72),
    C5D12xlarge("c5d.12xlarge", 48),
    C5D24xlarge("c5d.24xlarge", 96),
    C5D2xlarge("c5d.2xlarge", 8),
    C5D4xlarge("c5d.4xlarge", 16),
    C5D9xlarge("c5d.9xlarge", 36),
    C5Dlarge("c5d.large", 2),
    C5Dxlarge("c5d.xlarge", 4),

    M5D12xlarge("m5d.12xlarge", 48),
    M5D16xlarge("m5d.16xlarge", 64),
    M5D24xlarge("m5d.24xlarge", 96),
    M5D2xlarge("m5d.2xlarge", 8),
    M5D4xlarge("m5d.4xlarge", 16),
    M5Dlarge("m5d.large", 2),
    M5Dxlarge("m5d.xlarge", 4),

    M5DNlarge("m5dn.large", 2),
    M5DNxlarge("m5dn.xlarge", 4),
    M5DN2xlarge("m5dn.2xlarge", 8),
    M5DN4xlarge("m5dn.4xlarge", 16),
    M5DN8xlarge("m5dn.8xlarge", 32),
    M5DN12xlarge("m5dn.12xlarge", 48),
    M5DN16xlarge("m5dn.16xlarge", 64),
    M5DN24xlarge("m5dn.24xlarge", 96),
    M5DNmetal("m5dn.metal", 96),

    M5Nlarge("m5n.large", 2),
    M5Nxlarge("m5n.xlarge", 4),
    M5N2xlarge("m5n.2xlarge", 8),
    M5N4xlarge("m5n.4xlarge", 16),
    M5N8xlarge("m5n.8xlarge", 32),
    M5N12xlarge("m5n.12xlarge", 48),
    M5N16xlarge("m5n.16xlarge", 64),
    M5N24xlarge("m5n.24xlarge", 96),
    M5Nmetal("m5n.metal", 96),

    R5Nlarge("r5n.large", 2),
    R5Nxlarge("r5n.xlarge", 4),
    R5N2xlarge("r5n.2xlarge", 8),
    R5N4xlarge("r5n.4xlarge", 16),
    R5N8xlarge("r5n.8xlarge", 32),
    R5N12xlarge("r5n.12xlarge", 48),
    R5N16xlarge("r5n.16xlarge", 64),
    R5N24xlarge("r5n.24xlarge", 96),
    R5Nmetal("r5n.metal", 96),

    R5DNlarge("r5dn.large", 2),
    R5DNxlarge("r5dn.xlarge", 4),
    R5DN2xlarge("r5dn.2xlarge", 8),
    R5DN4xlarge("r5dn.4xlarge", 16),
    R5DN8xlarge("r5dn.8xlarge", 32),
    R5DN12xlarge("r5dn.12xlarge", 48),
    R5DN16xlarge("r5dn.16xlarge", 64),
    R5DN24xlarge("r5dn.24xlarge", 96),
    R5DNmetal("r5dn.metal", 96),

    M5Alarge("m5a.large", 2),
    M5Axlarge("m5a.xlarge", 4),
    M5A2xlarge("m5a.2xlarge", 8),
    M5A4xlarge("m5a.4xlarge", 16),
    M5A8xlarge("m5a.8xlarge", 32),
    M5A12xlarge("m5a.12xlarge", 48),
    M5A24xlarge("m5a.24xlarge", 96),

    P4D("p4d.24xlarge", 96),

    R5Alarge("r5a.large", 2),
    R5Axlarge("r5a.xlarge", 4),
    R5A2xlarge("r5a.2xlarge", 8),
    R5A4xlarge("r5a.4xlarge", 16),
    R5A12xlarge("r5a.12xlarge", 48),
    R5A24xlarge("r5a.24xlarge", 96),

    R5Dlarge("r5d.large", 2),
    R5Dxlarge("r5d.xlarge", 4),
    R5D2xlarge("r5d.2xlarge", 8),
    R5D4xlarge("r5d.4xlarge", 16),
    R5D12xlarge("r5d.12xlarge", 48),
    R5D24xlarge("r5d.24xlarge", 96),

    I3metal("i3.metal", 72),
    R512xlarge("r5.12xlarge", 48),
    R524xlarge("r5.24xlarge", 96),
    R52xlarge("r5.2xlarge", 8),
    R54xlarge("r5.4xlarge", 16),
    R58xlarge("r5.8xlarge", 32),
    R5large("r5.large", 2),
    R5xlarge("r5.xlarge", 4),
    Z1D12xlarge("z1d.12xlarge", 48),
    Z1D2xlarge("z1d.2xlarge", 8),
    Z1D3xlarge("z1d.3xlarge", 12),
    Z1D6xlarge("z1d.6xlarge", 24),
    Z1Dlarge("z1d.large", 2),
    Z1Dxlarge("z1d.xlarge", 4),
    T32xlarge("t3.2xlarge", 8),
    T3large("t3.large", 2),
    T3medium("t3.medium", 2),
    T3micro("t3.micro", 2),
    T3nano("t3.nano", 2),
    T3small("t3.small", 2),
    T3xlarge("t3.xlarge", 4),
    G3Sxlarge("g3s.xlarge", 4),
    C5N18xlarge("c5n.18xlarge", 72),
    C5N2xlarge("c5n.2xlarge", 8),
    C5N4xlarge("c5n.4xlarge", 16),
    C5N9xlarge("c5n.9xlarge", 36),
    C5Nlarge("c5n.large", 2),
    C5Nxlarge("c5n.xlarge", 4),
    A12xlarge("a1.2xlarge", 8),
    A14xlarge("a1.4xlarge", 16),
    A1large("a1.large", 2),

    T3A2xlarge("t3a.2xlarge", 8),
    T3Alarge("t3a.large", 2),
    T3Amedium("t3a.medium", 2),
    T3Amicro("t3a.micro", 2),
    T3Anano("t3a.nano", 2),
    T3Asmall("t3a.small", 2),
    T3Axlarge("t3a.xlarge", 4),

    I3EN12xlarge("i3en.12xlarge", 48),
    I3EN24xlarge("i3en.24xlarge", 96),
    I3EN2xlarge("i3en.2xlarge", 8),
    I3EN3xlarge("i3en.3xlarge", 12),
    I3EN6xlarge("i3en.6xlarge", 24),
    I3ENlarge("i3en.large", 2),
    I3ENxlarge("i3en.xlarge", 4),
    I3ENMetal("i3en.metal", 96),
    R5AD12xlarge("r5ad.12xlarge", 48),
    R5AD24xlarge("r5ad.24xlarge", 96),
    R5AD2xlarge("r5ad.2xlarge", 8),
    R5AD4xlarge("r5ad.4xlarge", 16),
    R5ADlarge("r5ad.large", 2),
    R5ADxlarge("r5ad.xlarge", 4),
    M5AD12xlarge("m5ad.12xlarge", 48),
    M5AD24xlarge("m5ad.24xlarge", 96),
    M5AD2xlarge("m5ad.2xlarge", 8),
    M5AD4xlarge("m5ad.4xlarge", 16),
    M5ADlarge("m5ad.large", 2),
    M5ADxlarge("m5ad.xlarge", 4),

    M6Gmedium("m6g.medium", 1),
    M6Glarge("m6g.large", 2),
    M6Gxlarge("m6g.xlarge", 4),
    M6G2xlarge("m6g.2xlarge", 8),
    M6G4xlarge("m6g.4xlarge", 16),
    M6G8xlarge("m6g.8xlarge", 32),
    M6G12xlarge("m6g.12xlarge", 48),
    M6G16xlarge("m6g.16xlarge", 64),
    M6GMetal("m6g.metal", 64),

    C5ALarge("c5a.large", 2),
    C5AXlarge("c5a.xlarge", 4),
    C5A2xlarge("c5a.2xlarge", 8),
    C5A4xlarge("c5a.4xlarge", 16),
    C5A8xlarge("c5a.8xlarge", 32),
    C5A12xlarge("c5a.12xlarge", 48),
    C5A16xlarge("c5a.16xlarge", 64),
    C5A24xlarge("c5a.24xlarge", 96),

    C5ADlarge("c5ad.large",3),
    C5ADxlarge("c5ad.xlarge",4),
    C5AD2xlarge("c5ad.2xlarge",8),
    C5AD4xlarge("c5ad.4xlarge",16),
    C5AD8xlarge("c5ad.8xlarge",32),
    C5AD12xlarge("c5ad.12xlarge",48),
    C5AD16xlarge("c5ad.16xlarge",64),
    C5AD24xlarge("c5ad.24xlarge",96),

    C6GMedium("c6g.medium", 1),
    C6GLarge("c6g.large", 2),
    C6GXlarge("c6g.xlarge", 4),
    C6G2xlarge("c6g.2xlarge", 8),
    C6G4xlarge("c6g.4xlarge", 16),
    C6G8xlarge("c6g.8xlarge", 32),
    C6G12xlarge("c6g.12xlarge", 48),
    C6G16xlarge("c6g.16xlarge", 64),
    C6GMetal("c6g.metal", 64),

    R6GMedium("r6g.medium", 1),
    R6GLarge("r6g.large", 2),
    R6GXlarge("r6g.xlarge", 4),
    R6G2xlarge("r6g.2xlarge", 8),
    R6G4xlarge("r6g.4xlarge", 16),
    R6G8xlarge("r6g.8xlarge", 32),
    R6G12xlarge("r6g.12xlarge", 48),
    R6G16xlarge("r6g.16xlarge", 64),
    R6GMetal("r6g.metal", 64),

    M6GDmedium("m6gd.medium", 1),
    M6GDlarge("m6gd.large", 2),
    M6GDxlarge("m6gd.xlarge", 4),
    M6GD2xlarge("m6gd.2xlarge", 8),
    M6GD4xlarge("m6gd.4xlarge", 16),
    M6GD8xlarge("m6gd.8xlarge", 32),
    M6GD12xlarge("m6gd.12xlarge", 48),
    M6GD16xlarge("m6gd.16xlarge", 64),
    M6GDMetal("m6gd.metal", 64),

    C6GDMedium("c6gd.medium", 1),
    C6GDLarge("c6gd.large", 2),
    C6GDXlarge("c6gd.xlarge", 4),
    C6GD2xlarge("c6gd.2xlarge", 8),
    C6GD4xlarge("c6gd.4xlarge", 16),
    C6GD8xlarge("c6gd.8xlarge", 32),
    C6GD12xlarge("c6gd.12xlarge", 48),
    C6GD16xlarge("c6gd.16xlarge", 64),
    C6GDMetal("c6gd.metal", 64),

    D3ENXlarge("d3en.xlarge", 4),
    D3EN2xlarge("d3en.2xlarge", 8),
    D3EN4xlarge("d3en.4xlarge", 16),
    D3EN6xlarge("d3en.6xlarge", 24),
    D3EN8xlarge("d3en.8xlarge", 32),
    D3EN12xlarge("d3en.12xlarge", 48),

    D3Xlarge("d3.xlarge", 4),
    D32xlarge("d3.2xlarge", 8),
    D34xlarge("d3.4xlarge", 16),
    D38xlarge("d3.8xlarge", 32),

    R5BLarge("r5b.large", 2),
    R5BXlarge("r5b.xlarge", 4),
    R5B2xlarge("r5b.2xlarge", 8),
    R5B4xlarge("r5b.4xlarge", 16),
    R5B8xlarge("r5b.8xlarge", 32),
    R5B12xlarge("r5b.12xlarge", 48),
    R5B16xlarge("r5b.16xlarge", 64),
    R5B24xlarge("r5b.24xlarge", 96),
    R5BMetal("r5b.metal", 96),

    M5ZNLarge("m5zn.large", 2),
    M5ZNXlarge("m5zn.xlarge", 4),
    M5ZN2xlarge("m5zn.2xlarge", 8),
    M5ZN3xlarge("m5zn.3xlarge", 12),
    M5ZN6xlarge("m5zn.6xlarge", 24),
    M5ZN12xlarge("m5zn.12xlarge", 48),
    M5ZNMetal("m5zn.metal", 48),

    R6GDMedium("r6gd.medium", 1),
    R6GDLarge("r6gd.large", 2),
    R6GDXlarge("r6gd.xlarge", 4),
    R6GD2xlarge("r6gd.2xlarge", 8),
    R6GD4xlarge("r6gd.4xlarge", 16),
    R6GD8xlarge("r6gd.8xlarge", 32),
    R6GD12xlarge("r6gd.12xlarge", 48),
    R6GD16xlarge("r6gd.16xlarge", 64),
    R6GDMetal("r6gd.metal", 64),

    G4AD4xlarge("g4ad.4xlarge", 16),
    G4AD8xlarge("g4ad.8xlarge", 32),
    G4AD16xlarge("g4ad.16xlarge", 64);

    private String  value;
    private Integer executors;
    private static final Logger LOGGER = LoggerFactory.getLogger(AwsInstanceTypeEnum.class);

    private AwsInstanceTypeEnum(String value, Integer executors) {
        this.value = value;
        this.executors = executors;
    }

    public String toString() {
        return this.value;
    }

    public Integer getExecutors() {
        return executors;
    }

    public static AwsInstanceTypeEnum fromValue(String value) {
        AwsInstanceTypeEnum retVal = null;
        for (AwsInstanceTypeEnum instanceType : AwsInstanceTypeEnum.values()) {
            if (instanceType.value.equals(value)) {
                retVal = instanceType;
                break;
            }
        }

        if (retVal == null) {
            LOGGER.error("Tried to create instance type enum for: " + value + ", but we don't support such type ");
        }

        return retVal;
    }

    public String getValue() {
        return value;
    }
}
